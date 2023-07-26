<template>
  <canvas ref="canvas" @wheel="scroll" @mousedown="startDrag" @mousemove="drag" @mouseup="stopDrag"></canvas>
</template>


<script>
import axios from 'axios';

export default {
  name: "TimeLine",
  data() {
    return {
      dragging: false,
      offset: 0,
      startMouseX: null,
      frameData: {},
      currentTime: null,
      middleId: null,
      gap: 100,
      timeGap: 1,
      ctx: null,
      canvas: null,
    }
  },
  props: {
    initialTime: {
      type: Number,
      default: null
    },
    markedZone: {
      type: Array,
      default: function () {
        return [];
      }
    },
    markedFrame: {
      type: Array,
      default: function () {
        return [];
      }
    }
  },
  computed: {},
  methods: {
    init() {
      this.middleId = this.initialTime;
      this.canvas = this.$refs.canvas;
      this.ctx = this.canvas.getContext('2d');
      this.canvas.width = this.$el.clientWidth;
      this.canvas.height = this.$el.clientHeight;
    },
    async fetchTimeFrames() {
      let response = await axios.get('http://localhost:8080/frameTime');
      this.frameData = response.data;
    },
    scroll(event) {
      if (event.deltaY < 0) {
        this.gap /= 1.1;
        if (this.gap < 55) {
          this.gap = 55;
          this.timeGap *= 2;
        }
      } else {
        if (this.timeGap > 1) {
          this.timeGap /= 2;
        } else {
          this.gap *= 1.1;
          if (this.gap > 100) {
            this.gap = 100;
          }
        }
      }
      this.draw();
    },
    startDrag(event) {
      this.dragging = true;
      this.startMouseX = event.clientX;
      window.addEventListener('mousemove', this.drag);
      window.addEventListener('mouseup', this.stopDrag);
    },
    drag(event) {
      if (this.dragging) {
        // 计算当前鼠标位置与开始拖动时的位置差，即拖动的相对偏移
        let deltaX = event.clientX - this.startMouseX;
        // 更新开始拖动时的鼠标位置，为下一次移动计算偏移量做准备
        if (Math.abs(deltaX) > this.canvas.width / (this.canvas.width / this.gap + 1)) {
          this.startMouseX = event.clientX;
          let keys = Object.keys(this.frameData).map(key => Number(key));
          let minKey = Math.min(...keys);
          let maxKey = Math.max(...keys);
          if (deltaX > 0) {
            this.middleId -= this.timeGap;
            this.$emit('last-frame', this.timeGap);
          } else {
            this.middleId += this.timeGap;
            this.$emit('next-frame', this.timeGap);
          }
          if (this.middleId < minKey) {
            this.middleId = minKey;
          }
          if (this.middleId > maxKey) {
            this.middleId = maxKey;
          }
        }
        this.draw();
      }
    },
    stopDrag() {
      this.dragging = false;
      window.removeEventListener('mousemove', this.drag);
      window.removeEventListener('mouseup', this.stopDrag);
      this.$emit('change-time', this.middleId);
    },
    drawLine(x1, y1, x2, y2, lineWidth = 1, color = 'black') {
      this.ctx.strokeStyle = color
      this.ctx.lineWidth = lineWidth
      this.ctx.beginPath()
      this.ctx.moveTo(x1, y1)
      this.ctx.lineTo(x2, y2)
      this.ctx.stroke()
    },
    draw() {
      if (!this.ctx) {
        console.warn('Context not initialized yet');
        return;
      }
      this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);


      // 初始化
      let start = this.middleId - Math.floor((this.canvas.width / 2 / this.gap)) * this.timeGap;
      let end = this.middleId + Math.floor((this.canvas.width / 2 / this.gap)) * this.timeGap;
      start = Math.max(start, Math.min(...Object.keys(this.frameData).map(Number)))
      end = Math.min(end, Math.max(...Object.keys(this.frameData).map(Number)))
      //绘制搜索结果可能存在的区域
      for (let highLight of this.markedZone) {
        let startHighLight = highLight[0]
        let endHighLight = highLight[1]
        if (startHighLight < end && endHighLight > start) {
          const x = (startHighLight - this.middleId) / this.timeGap * this.gap + this.canvas.width / 2;
          let color = this.ctx.fillStyle
          this.ctx.fillStyle = 'rgba(0, 128, 0, 0.5)';
          this.ctx.fillRect(x, 0, this.gap * (endHighLight - startHighLight), this.canvas.height / 2);
          this.ctx.fillStyle = color;
        }
      }
      for (let now = start; now <= end; now++) {
        const x = (now - this.middleId) / this.timeGap * this.gap + this.canvas.width / 2;
        if (this.markedFrame.includes(now)) {
          this.drawLine(x, 0, x, this.canvas.height / 2, 2, 'red')
        }
      }
      // 绘制刻度和文字
      this.ctx.font = '16px Arial';
      this.ctx.textAlign = 'center';
      for (let now = start; now <= end; now += this.timeGap) {
        const x = (now - this.middleId) / this.timeGap * this.gap + this.canvas.width / 2;
        if (!this.markedFrame.includes(now)) {
          this.drawLine(x, 0, x, this.canvas.height / 2, 1, 'black')
        }
        let time = this.frameData[now];
        if (time === undefined) {
          continue;
        }
        let date = new Date(time);
        let minutes = date.getMinutes();
        let seconds = date.getSeconds();
        this.ctx.fillText(`${minutes}:${seconds < 10 ? '0' + seconds : seconds}`, x, this.canvas.height * 3 / 4);
      }
      //绘制中间的线
      this.drawLine(this.canvas.width / 2, 0, this.canvas.width / 2, this.canvas.height / 4 * 2.5, 5)
      //绘制时间
      let time = this.frameData[this.middleId];
      let date = new Date(time);
      let year = date.getFullYear();
      let month = (date.getMonth() + 1).toString().padStart(2, '0');
      let day = date.getDate().toString().padStart(2, '0');
      let hours = date.getHours().toString().padStart(2, '0');
      this.ctx.fillText(year + '年' + month + '月' + day + '日' + hours + '时', this.canvas.width / 2, this.canvas.height-8 );
    },
    setId(id) {
      if (id === this.middleId) {
        return;
      }
      this.middleId = id;
      this.draw();
    },
    onResize() {
      const {width, height} = this.$el.getBoundingClientRect();
      const canvas = this.$refs.canvas;
      canvas.width = width;
      canvas.height = height;
      this.draw();
    },
  },
  mounted() {
    this.$nextTick(async () => {
      this.init()
      await this.fetchTimeFrames()
      if (this.initialTime === null) {
        let keys = Object.keys(this.frameData).map(Number);
        this.middleId = Math.max(...keys);
      }
      console.log('timeline' + this.middleId)
      this.$emit('change-time', this.middleId);
      this.draw()
      window.addEventListener('resize', this.onResize);
      this.onResize();
    })
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.onResize);
  },

}
</script>
