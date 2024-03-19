# VideoApp
这是一个连接服务器的视频播放器：
- 将所需要的视频存放在可访问到的服务器上
- 使用里面的python工程处理一个json文件，存放到app本地，里面包含url、文件名、类别啥的
- 后续要进行改进不在使用脚本python文件

## 2023-10-15
更新
- 根据MVP模式重构了项目
- 设计分批从服务器加载视频的描述算法，解决一开始等待时间过长的问题
  - 使用了SmartRefreshLayout上拉下拉加载实时数据
  - 并且用一个map缓存数据
 
## 2023-11-05
更新
- 添加头部的bar来给分类，并且更改json文件的格式
- 增加二级recycler view 使首页的视频描述可以有一个分类的效果

## 2024-3-19
更新（新增master-web分支）
- 将资源转移到springboot的服务器上：https://github.com/czg19970914/VideoServer/tree/master
- 使用Retrofit访问资源
- 使用LRU算法来缓存数据
