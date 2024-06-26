**AMap:**

配置(零):

> 1.下载SDK, 在app添加sourceSets语句; 在manifest添加services,
> 权限和访问key(application标签)
>
> 2.建MapApplication类同意隐私政策(放application标签的name里)
>
> 3.用devrel:easypermissions库申请权限, 将权限加进列表, 首先检查版本,
> 请求权限, 重写onRequestPermissionsResult再请求一次权限
>
> 4\. 经纬 -\> LatLng -\> LatLonPoint

MainActivity:

1.  初始化定位

```{=html}

```

1.  添加client和clientOption两个变量, 添加initLocation方法,
    配置参数(放到检查版本之前, 防止空指针)

2.  在低版本和获得权限后的高版本client.startLocation启动定位

3.  继承位置listener, 重写locationChanged, 位置改变后获得位置,
    获得位置后client.stop, destroy活动后也destroy client

```{=html}

```

2.  显示地图(显示当前定位地图)

```{=html}

```

1.  Xml添加mapView, onCreate用mapView. onCreate(savedInstanceState),
    destroy活动后也destroy mapView

2.  添加三个地图生命周期管理方法

3.  新增AMap和位置更改Listener两个变量, 新增initMap方法, 配置,
    方法放在初始化定位后. mapView.
    onCreate(savedInstanceState)不要有两句不然会有北京地图

4.  继承位置Source, 重写激活定位和停止定位方法

5.  调用位置更改Listener的位置更改方法,
    传入位置(在初始化拿到位置之后的地方)

```{=html}

```

3.  地图设置

```{=html}

```

1. New一个位置style, 在initMap配置, 在set一下,
   顺便设置16缩放等级\--\[3, 20\]

2. New一个uisetting, 隐藏缩放按钮, 显示比例尺

3. POI

   a.  用google-material库在xml添加浮动按钮(加drawable), 一点触发函数

   b.  加4变量(query, search, citycode, fabPOI)

   c.  在获取定位出fabPOI.show, 并赋值citycode

   d.  添加queryPOI方法, 配置

   e.  继承POIsearch监听, 重写两个方法, 其中一个可以拿到POI数据

```{=html}

```

4.  地图点击长按事件

```{=html}

```

1. InitMap设置监听点击和长按(点击是能拿到具体坐标的)

2. 继承点击和长按监听, 重写点击和长按方法

3. 坐标转地址(地址转坐标)---点击地图出坐标

   a.  创建geocodeSearch和解析成功表示码两个变量,
       并在InitMap中构建对象, 设置监听

   b.  继承geocodeSearch监听, 重写两个方法, 分别是地址坐标互转

   c.  创建latlonToAddress方法(传入LatLng), 异步获取地址

   d.  回调方法判断是不是SUCCESS码, 是的话直接拿到地址

   e.  (有LatLng的地方都可以调用方法转成地址, 点击和长按都可以)

4. 地址转坐标---输入框输入地名确认得到坐标

   a.  Xml加个toolbar加个edittext

   b.  android:imeOptions=\"actionSearch点击输入法确定直接搜索,
       要设置键盘监听, 继承键盘key监听, 重写onKey方法,
       (判断enter&&up)方法里添加收缩键盘, 然后new GeocodeQuery,
       (加入city)启动异步地址转坐标

   c.  创建city字符串变量, 在初始化坐标的地方初始化

   d.  回调方法判断是不是SUCCESS码, 再判断空值拿第一个值, get到坐标,
       get经纬度

5. 根据点击地图得到经纬度, 用经纬度绘制标点Marker

   a.  直接在onMapClick下添加aMap.addMarker

   b.  Xml添加浮动按钮-清空Marker, 点击地图时显示, 点击触发方法

   c.  初始化删除按钮和标点列表(点击就加进去addMarker),
       建立清空函数(遍历列表, 一个个remove然后隐藏浮标)

6. 绘制动画效果Marker

   a.  在addMarker添加动画效果, 导包要导高德的

   b.  Marker的点击

       i.  继承Marker点击监听, 在initMap中设置点击事件,
           重写onMarkerClick

   c.  Marker的拖拽

       i.  继承Marker拖拽监听, 在initMap中设置拖拽事件, 重写3个方法
       
       ii. 在addMarker添加.draggable(true)

   d.  在addMarker添加标点装饰: .title, .snippet,
       然后在onMarkerClick设置showInfoWindow(isShow判断),
       想要绘制时显示可以在addMarker上show

       i.  新建xml, 往xml(背景用图片)加一个图片两个text,
           android:ellipsize=\"end\"过长会显示...
       
       ii. 继承InfoWindowAdapter, 在InitMap设置inFoWindowAdapter监听,
           重写两个方法, 添加渲染方法,
           创建视图并和Marker在两个方法里渲染---得到了标点的渲染描述
       
       iii. 继承InfoWindow监听, 在InitMap设置infoWindow点击事件,
            重写infoWindow点击方法---实现infoWindow点击监听

7. 随着点击改变地图中心点

   a.  新增updateMapCenter方法, 配置, 在onMapClick的addMarker后调用

   b.  添加动画使移动顺滑要修改updateMapCenter的move改为animate

RouteActivity:

5.  出行路线规划准备

```{=html}

```

1.  Xml加个mapView, activity添加初始化定位加地图的所有方法

2.  MainActivity添加一个跳转浮标, 直接点击函数跳转

```{=html}

```

6.  步行路线规划

```{=html}

```

1. 创建两个LatLonPoint对象(起点终点), 添加util创建字符串类

2. 添加MapUtil类, LatLng和LatLonPoint互换, 秒换单位, 米换单位

3. 经纬 -\> LatLng -\> LatLonPoint

4. 在初始化定位处, 用经纬赋值起点

5. 继承MapClick接口, initMap设置地图点击监听, 重写mapClick方法,
   赋值终点

6. 新建RouteSearch对象, 继承RouteSearch监听接口, 新增initRoute方法,
   在方法内赋值routeSearch并设置监听, 在onCreate调用.
   重写四(4种出行)方法

7. 新增startRouteSearch方法(分别绘制起终点, 搜索路线,
   构建步行路线搜索对象, 异步路径规划查询), 在onMapClick调用

8. 新增overlay包, 增3类

   a.  AMapServicesUtil地图服务工具类

       i.  有一个方法根据给定因子缩放图片

   b.  RouteOverlay路线图层叠加

       i.  更改渲染吧

   c.  WalkRouteOverlay步行路线图层类(继承RouteOverlay)

9. 回到RouteActivity, 修改onWalkRouteSearched(先清楚覆盖物)

```{=html}

```

7.  骑行路线规划

```{=html}

```

1.  修改RouteActivity的xml, 改为Relative, 新建Linear,
    加个text和spinner(选择控件)

2.  在RouteActivity创建出行方式String数组, 出行方式Int值,
    ArrayAdapter\<String\>(配置下拉框数据)3个对象

3.  新建initTravelMode, 配置(adapter,
    spinner监听(刚好position0对应步行Int)), 在onCreate调用

4.  修改startRouteSearch根据不同出行Int值构建不同路线对象和计算路径

5.  MapUtil新增方法将集合体的LatLonPoint转换为相应LatLng

6.  在overlay包新增RideRouteOverlay类绘制步行图层

7.  在RouteActivity修改onRideRouteSearched

```{=html}

```

8.  驾车路线规划

```{=html}

```

1.  出行String数组加个值,
    修改startRouteSearch根据不同出行Int值构建不同路线对象和计算路径

2.  在overlay包新增DrivingRouteOverlay类

3.  在RouteActivity修改onDriveRouteSearched

```{=html}

```

9.  公交路线规划

    1.  出行String数组加个值,
        修改startRouteSearch根据不同出行Int值构建不同路线对象和计算路径

    2.  新增city, 在初始化定位赋值city,
        z在startRouteSearch构建公交路线时传入city

    3.  在overlay包新增BusRouteOverlay

RouteDetailActivity:

10. 步行路线详情(小Demo)

    1.  修改activity_route加个相对布局加按钮和两个文字,
        在routeactivity配置布局

    2.  修改onWalkRouteSearched, 添加步行时间,
        设置跳转(putExtra放type(Int), walkPath)

11. 详情配置:

    1. Style.xml添加详情页面主题,
       在manifest中的activity找到详情的设置theme

    2. 创建RouteDetailActivity

       a.  加个toolbar(设置navigationIcon为黑箭头),
           加个分割线(View里height=0.5dp就弄出来了),
           加个LinearLayout(text, imageVIew(1dp虚线),
           recyclerView(随便配置一下))

       b.  添加initView, 配置视图, 设置navigation回退.
           根据getExtra的Int做不同路线

       c.  添加帮助框架, settings.gradle 加一句maven{url
           "https://jitpack.io"}, 在app
           gradle用CymChad:BaseRecyclerViewAdapterHelper库

       d.  新建item_segment(RelativeLayout)

           i.  ImageView(bus_seg_split_line)分割线
           
           ii. RelativeLayout
           
               1.  三个image, dir(起点), up, down
           
           iii. RelativeLayout
           
                1.  RelativeLayout
           
                    a.  一个Image(bus_expand_image)一个text(bus_station_num),
           
                2.  Text, bus_line_name(出发)
           
           iv. LinearLayout, id(expand_content)

    3. 新建adapter包, 建WalkSegmentListAdapter继承框架类

       a.  构造方法拿到WalkStep集合

       b.  重写convert方法,
           分别判断数组是第一个?中间?最后一个?来改变视图

    4. 在MapUtil添加(骑行代码也是用getWalk)getWalkActionID(在convert中间方法使用:
       根据WalkStep拿到的名字来判断返回的图片引用)

12. 步行路线详情

    1.  新建walkDetail方法, getExtra拿到path, 设置视图和setAdapter

    2.  回到activity, 在根据getExtra的Int做不同路线那里(iniView)的case 0
        调用walkDetail方法, onCreate调用initView

13. 骑行路线详情

    1. 修改onRideRouteSearched, 添加骑行时间,
       设置跳转(putExtra放type(Int), ridePath)

    2. 建RideSegmentListAdapter继承框架类

       a.  构造方法拿到RideStep集合

       b.  重写convert方法,
           分别判断数组是第一个?中间?最后一个?来改变视图

    3. 新建rideDetail方法, getExtra拿到path, 设置视图和setAdapter

    4. 回到activity, 在根据getExtra的Int做不同路线那里(iniView)的case 1
       调用rideDetail方法

14. 驾车路线详情

    1. 修改onDriveRouteSearched, 添加驾车时间,
       设置跳转(putExtra放type(Int), ridePath)

    2. 建DriveSegmentListAdapter继承框架类

       a.  构造方法拿到DriveStep集合

       b.  重写convert方法,
           分别判断数组是第一个?中间?最后一个?来改变视图

       c.  在MapUtil添加(骑行代码也是用getWalk)getDriveActionID(在convert中间方法使用:
           根据DriveStep拿到的名字来判断返回的图片引用

    3. 新建driveDetail方法, getExtra拿到path, 设置视图和setAdapter

    4. 回到activity, 在根据getExtra的Int做不同路线那里(iniView)的case 1
       调用driveDetail方法

15. 公交路线详情(最难)

    1. 修改onBusRouteSearched, 添加公交时间,
       设置跳转(putExtra放type(Int), ridePath)

    2. 新建item_segment_ex.xml(LinearLayout, horizontal)

       a.  RelativeLayout

           i.  三个image, dir, up, down

       b.  Text(bus_line_station_name)

    3. 在util包新增SchemeBusStep类, 不用原来的BusStep而是封装一下

    4. 建BusSegmentListAdapter继承框架类

       a.  构造方法拿到BusStep集合

       b.  重写convert方法, 分别判断数组是第一个?中间(有步行, 公交,
           地铁, 打车(Taxi))?最后一个?来改变视图

       c.  BusItem点一下还能显示更多(公交, 火车),
           添加两个添加公交车和火车站方法, 加一个获取铁路时间方法

       d.  在activity新建getBusSteps方法对BusStep组装

    5. 新建busDetail方法, getExtra拿到path, 设置视图和setAdapter

    6. 回到activity, 在根据getExtra的Int做不同路线那里(iniView)的case 1
       调用busDetail方法

16. 手动输入目的地和出发地

    1. 修改activity_route.xml(RelativeLayout)

       a.  LinearLayout

           i.  LinearLayout
           
               1.  一个text一个spinner
           
           ii. RelativeLayout(软键盘变搜索)
           
               1.  一个text一个edittext
           
               2.  一个0.5dp分割线
           
               3.  一个text一个edittext
           
               4.  一个0.5dp分割线

       b.  ...

    2. 修改Routeactivity, 在initTravelMode初始化视图,
       添加软键盘按键监听

    3. 在初始化定位处setText在etStartAddres设为当前地(设置不可改)

    4. Activity继承软键盘按键监听, 重写onKey方法, 判断enter&up
       然后两句隐藏软键盘

    5. 新建GeocodeSearch(地理编码搜索), SUCCESS(解析成功标识码)对象,
       在initMap构建对象和设置监听

    6. 继承GeocodeSearch监听, 重写两个方法, 先地址转坐标-如果是成功码,
       拿到终点, startRouteSearch(根据case四个int选择不同出行)

    7. 修改onKey, 新加query(输入终点和城市),
       geocodeSearch.getFromLocationNameAsyn(query)

17. 手动输入出发地

    1.  修改activity_route.xml,
        修改一下第一个edittext和第二个保持一致(actionSearch)

    2.  回到activity注释掉禁用

    3.  定义地址String变量, 定义tag(Int)为-1(起点转坐标为1)

    4.  在initTravelMode设置起点输入框监听

    5.  修改onKey, 先获取两输入框值, 判空,
        判断出发地是否更改来设置tag(不等直接-新加query(输入起点和城市),
        geocodeSearch.getFromLocationNameAsyn(query)), 后面不变同上

    6.  修改onGeocodeSearched,
        判断是不是更改了起点(因为更改了起点的话就是起点模式搜索,
        否则是终点模式(有bug, 你必须一开始先输入终点搜索,
        否则没有终点了, 你一开始先输入起点的话就完了没终点了)),
        更改了起点的话输入第一个点就是起点否则是终点
