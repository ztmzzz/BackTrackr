import {createRouter, createWebHashHistory} from 'vue-router'
import VideoPlayer from './components/VideoPlayer.vue'
import SearchBox from "@/components/SearchBox.vue"

const routes = [
    {path: '/search', component: SearchBox},
    {path: '/video', component: VideoPlayer}

]

const router = createRouter({
    history: createWebHashHistory(),
    routes
})

export default router
