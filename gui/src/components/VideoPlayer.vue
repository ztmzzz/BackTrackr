<template>
  <el-row justify="center" align="middle" style="height: 70vh;">
    <canvas ref="canvas" style="position: absolute; top: 0; left: 0; z-index: 1;pointer-events:none;"></canvas>
    <video ref="video" controls preload="auto" style="max-width: 100%; max-height: 100%;"
           @timeupdate="updateTime" @loadedmetadata="initCanvas" @mousemove="showControls"
           @mouseleave="hideControls">
      <source :src="videoSrc" type="video/mp4">
    </video>
  </el-row>
  <el-row style="height: 15vh;">
    <el-col :span="24">
      <TimeLine ref="timeline" v-if="!loading" @change-time="setTime" @next-frame="nextFrame" @last-frame="lastFrame"
                style="width: 100%;height: 80%"
                :initial-time="this.searchData?this.searchData.periodId[0][0]:null"
                :marked-zone="this.searchData?this.searchData.periodId:undefined"
                :marked-frame="this.searchData?Object.keys(this.searchData.timeToTextBox).map(Number):undefined"
      ></TimeLine>
    </el-col>
  </el-row>
</template>


<script>
import TimeLine from "@/components/TimeLine.vue";
import axios from "axios";

export default {
  name: 'VideoPlayer',
  data() {
    return {
      videoSrc: '',
      rectangles: [],
      searchData: null,
      startId: null,
      searchText: null,
      loading: true,
    };
  },
  components: {TimeLine},
  methods: {
    async setTime(id) {
      let response = await axios.post('http://localhost:8080/videoInfo', {
        id: id
      });
      let data = response.data;
      let path = data.path;
      let time = data.time;
      time = Number(time)
      let newPath = 'http://localhost:8080/' + path;
      if (this.videoSrc !== newPath) {
        this.videoSrc = 'http://localhost:8080/' + path;
        this.$refs.video.load()
        await this.videoToStartId()
      }
      this.$refs.video.currentTime = time;
    },
    nextFrame(time) {
      this.$refs.video.currentTime += time;
    },
    lastFrame(time) {
      this.$refs.video.currentTime -= time;
    },
    drawRectangles() {
      let video = this.$refs.video;
      let canvas = this.$refs.canvas;
      let context = canvas.getContext('2d');
      // 清除canvas上的内容
      context.clearRect(0, 0, canvas.width, canvas.height);
      // 缩放比例
      let scaleX = video.videoWidth / video.clientWidth;
      let scaleY = video.videoHeight / video.clientHeight;
      if (!this.rectangles) {
        return;
      }
      this.rectangles.forEach(rect => {
        // 根据视频缩放比例缩放坐标
        let x = rect[0] / scaleX;
        let y = rect[1] / scaleY;
        let width = rect[2] / scaleX;
        let height = rect[3] / scaleY;
        // 绘制红色长方形
        context.strokeStyle = 'red';
        context.strokeRect(x, y, width, height);
      });
    },
    updateTime() {
      this.updateRectangles()
      this.updateTimeLine()
    },
    updateRectangles() {
      if (this.searchData) {
        let time = this.$refs.video.currentTime;
        let id = this.startId + time;
        this.rectangles = this.searchData.timeToTextBox[id]
      }

    },
    updateTimeLine() {
      let currentTime = this.$refs.video.currentTime;
      this.$refs.timeline.setId(this.startId + Math.floor(currentTime));
    },
    async videoToStartId() {
      let lastSlashIndex = this.videoSrc.lastIndexOf('/');
      let dotIndex = this.videoSrc.lastIndexOf('.');
      let dateStr = this.videoSrc.slice(lastSlashIndex + 1, dotIndex);
      let response = await axios.post('http://localhost:8080/videoToStartId', {
        videoPath: dateStr
      });
      this.startId = response.data;
    },
    initCanvas() {
      let video = this.$refs.video;
      let canvas = this.$refs.canvas;
      let videoRect = video.getBoundingClientRect();
      let parentRect = video.parentNode.getBoundingClientRect();
      let offsetX = videoRect.left - parentRect.left;
      let offsetY = videoRect.top - parentRect.top;

      // 将canvas元素的位置设置为与video元素相同的位置
      canvas.style.left = offsetX + 'px';
      canvas.style.top = offsetY + 'px';

      canvas.width = video.clientWidth;
      canvas.height = video.clientHeight;
    },
    async search() {
      let response = await axios.post('http://localhost:8080/search', {
        text: this.searchText
      })
      this.searchData = response.data
      this.loading = false
    },
    showControls() {
      this.$refs.video.controls = true;
    },
    hideControls() {
      this.$refs.video.controls = false;
    },
  },
  watch: {
    rectangles() {
      this.drawRectangles();
    },
  },
  created() {
    this.searchText = this.$route.query.searchText
    if (this.searchText) {
      this.search()
    } else {
      this.loading = false
    }
  },
  mounted() {
    window.addEventListener('resize', this.initCanvas);
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.initCanvas);
  },
}
</script>


<style scoped>

</style>
