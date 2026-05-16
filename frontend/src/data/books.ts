import coverHongLouMeng from '../assets/photos/红楼梦.png'
import coverSanGuo from '../assets/photos/三国演义.jpg'
import coverShuiHu from '../assets/photos/水浒传.jpg'
import coverXiYou from '../assets/photos/西游记.jpg'
import coverLuoTuo from '../assets/photos/骆驼祥子.jpg'
import coverWeiCheng from '../assets/photos/围城.jpg'
import coverNaHan from '../assets/photos/呐喊.jpg'
import coverHuoZhe from '../assets/photos/活着.png'
import coverBeiCan from '../assets/photos/悲惨世界.jpg'
import coverAnna from '../assets/photos/安娜・卡列尼娜.jpg'
import coverZuiYuFa from '../assets/photos/罪与罚.jpg'
import coverAoMan from '../assets/photos/傲慢与偏见.jpg'
import coverShuangCheng from '../assets/photos/双城记.jpg'
import coverLaoRen from '../assets/photos/老人与海.jpg'
import coverBianXing from '../assets/photos/变形记.jpg'
import coverZhuiYi from '../assets/photos/追忆似水年华.jpg'

export interface BookItem {
  title: string
  author: string
  rating: string
  reviews: number
  tag: string
  cover: string
}

export const featuredBooks: BookItem[] = [
  { title: '红楼梦', author: '曹雪芹', rating: '4.9', reviews: 1280, tag: '名著', cover: coverHongLouMeng },
  { title: '三国演义', author: '罗贯中', rating: '4.8', reviews: 980, tag: '名著', cover: coverSanGuo },
  { title: '水浒传', author: '施耐庵', rating: '4.7', reviews: 860, tag: '名著', cover: coverShuiHu },
  { title: '西游记', author: '吴承恩', rating: '4.9', reviews: 1500, tag: '名著', cover: coverXiYou }
]

export const libraryBooks: BookItem[] = [
  ...featuredBooks,
  { title: '骆驼祥子', author: '老舍', rating: '4.6', reviews: 640, tag: '名著', cover: coverLuoTuo },
  { title: '围城', author: '钱钟书', rating: '4.7', reviews: 720, tag: '名著', cover: coverWeiCheng },
  { title: '呐喊', author: '鲁迅', rating: '4.8', reviews: 890, tag: '名著', cover: coverNaHan },
  { title: '活着', author: '余华', rating: '4.8', reviews: 1100, tag: '名著', cover: coverHuoZhe },
  { title: '悲惨世界', author: '维克多·雨果', rating: '4.9', reviews: 2100, tag: '名著', cover: coverBeiCan },
  { title: '安娜·卡列尼娜', author: '列夫·托尔斯泰', rating: '4.8', reviews: 1880, tag: '名著', cover: coverAnna },
  { title: '罪与罚', author: '陀思妥耶夫斯基', rating: '4.8', reviews: 1720, tag: '名著', cover: coverZuiYuFa },
  { title: '傲慢与偏见', author: '简·奥斯汀', rating: '4.7', reviews: 1640, tag: '名著', cover: coverAoMan },
  { title: '双城记', author: '查尔斯·狄更斯', rating: '4.7', reviews: 1380, tag: '名著', cover: coverShuangCheng },
  { title: '老人与海', author: '欧内斯特·海明威', rating: '4.6', reviews: 1490, tag: '名著', cover: coverLaoRen },
  { title: '变形记', author: '弗兰茨·卡夫卡', rating: '4.6', reviews: 1260, tag: '名著', cover: coverBianXing },
  { title: '追忆似水年华', author: '马塞尔·普鲁斯特', rating: '4.5', reviews: 980, tag: '名著', cover: coverZhuiYi }
]

export const listBooks: BookItem[] = [
  { title: '红楼梦', author: '曹雪芹', rating: '4.9', reviews: 1280, tag: '名著', cover: coverHongLouMeng },
  { title: '水浒传', author: '施耐庵', rating: '4.7', reviews: 860, tag: '名著', cover: coverShuiHu },
  { title: '西游记', author: '吴承恩', rating: '4.9', reviews: 1500, tag: '名著', cover: coverXiYou },
  { title: '骆驼祥子', author: '老舍', rating: '4.6', reviews: 640, tag: '名著', cover: coverLuoTuo }
]

export const detailCover = coverHongLouMeng
