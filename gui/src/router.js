import {createRouter, createWebHashHistory} from 'vue-router'
import VideoPlayer from './components/VideoPlayer.vue'
import SearchBox from "@/components/SearchBox.vue"

const routes = [
    {path: '/search', component: SearchBox},
    {path: '/video', component: VideoPlayer},
    {path: '/', redirect: '/search'}

]

const router = createRouter({
    history: createWebHashHistory(),
    routes
})

export default router
