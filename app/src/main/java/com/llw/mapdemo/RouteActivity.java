package com.llw.mapdemo;

import static com.llw.mapdemo.util.MapUtil.convertToLatLng;
import static com.llw.mapdemo.util.MapUtil.convertToLatLonPoint;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RidePath;
import com.amap.api.services.route.RideRouteResult;

import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.llw.mapdemo.overlay.BusRouteOverlay;
import com.llw.mapdemo.overlay.DrivingRouteOverlay;
import com.llw.mapdemo.overlay.RideRouteOverlay;
import com.llw.mapdemo.overlay.WalkRouteOverlay;
import com.llw.mapdemo.util.MapUtil;

import java.util.List;

/**
 * 路线规划
 * @author llw
 */
public class RouteActivity extends AppCompatActivity implements
        AMapLocationListener,LocationSource, AMap.OnMapClickListener,
        RouteSearch.OnRouteSearchListener, View.OnKeyListener, GeocodeSearch.OnGeocodeSearchListener {

    private static final String TAG = "RouteActivity";
    //地图
    private MapView mapView;
    //地图控制器
    private AMap aMap = null;
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;
    //位置更改监听
    private OnLocationChangedListener mListener;
    //定义一个UiSettings对象
    private UiSettings mUiSettings;
    //定位样式
    private MyLocationStyle myLocationStyle = new MyLocationStyle();

    //起点
    private LatLonPoint mStartPoint;
    //终点
    private LatLonPoint mEndPoint;
    //路线搜索对象
    private RouteSearch routeSearch;
    //出行方式数组
    private static final String[] travelModeArray = {"步行出行", "骑行出行", "驾车出行", "公交出行"};

    //出行方式值
    private static int TRAVEL_MODE = 0;

    //数组适配器
    private ArrayAdapter<String> arrayAdapter;
    //城市
    private String city;
    //路线规划详情
    private RelativeLayout bottomLayout;
    //花费时间
    private TextView tvTime;

    //定位地址
    private String locationAddress;

    //起点、终点
    private EditText etStartAddress, etEndAddress;

    //地理编码搜索
    private GeocodeSearch geocodeSearch;
    //解析成功标识码
    private static final int PARSE_SUCCESS_CODE = 1000;

    //起点地址转坐标标识   1
    private int tag = -1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        //初始化定位
        initLocation();
        //初始化地图
        try {
            initMap(savedInstanceState);
        } catch (AMapException e) {
            throw new RuntimeException(e);
        }

        //启动定位
        mLocationClient.startLocation();
        //初始化路线
        initRoute(); //设置路线寻找监听器
        //初始化出行方式
        initTravelMode();
    }

    /**
     * 初始化定位
     */
    private void initLocation() {
        //初始化定位
        try {
            mLocationClient = new AMapLocationClient(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mLocationClient != null) {
            mLocationClient.setLocationListener(this);
            mLocationOption = new AMapLocationClientOption();
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //获取最近3s内精度最高的一次定位结果：
            //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
            mLocationOption.setOnceLocationLatest(true);
            mLocationOption.setNeedAddress(true);
            mLocationOption.setHttpTimeOut(20000);
            mLocationOption.setLocationCacheEnable(false);
            mLocationClient.setLocationOption(mLocationOption);
        }
    }

    /**
     * 初始化地图
     *
     * @param savedInstanceState
     */
    private void initMap(Bundle savedInstanceState) throws AMapException {
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        //初始化地图控制器对象
        aMap = mapView.getMap();
        //设置最小缩放等级为16 ，缩放级别范围为[3, 20]
        aMap.setMinZoomLevel(16);
        //开启室内地图
        aMap.showIndoorMap(true);
        //实例化UiSettings类对象
        mUiSettings = aMap.getUiSettings();
        //隐藏缩放按钮 默认显示
        mUiSettings.setZoomControlsEnabled(false);
        //显示比例尺 默认不显示
        mUiSettings.setScaleControlsEnabled(true);
        // 自定义定位蓝点图标
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.gps_point));
        //设置定位蓝点的Style
        aMap.setMyLocationStyle(myLocationStyle);
        // 设置定位监听
        aMap.setLocationSource(this);
        // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setMyLocationEnabled(true);
        //地图点击监听
        aMap.setOnMapClickListener((AMap.OnMapClickListener) this);

        //构造 GeocodeSearch 对象
        geocodeSearch = new GeocodeSearch(this);
        //设置监听
        geocodeSearch.setOnGeocodeSearchListener(this);


    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                //地址
                locationAddress = aMapLocation.getAddress();
                //获取纬度
                double latitude = aMapLocation.getLatitude();
                //获取经度
                double longitude = aMapLocation.getLongitude();
                Log.d(TAG, aMapLocation.getCity());
                Log.d(TAG,"nini"+locationAddress); //启动程序后要等一下, 再点进查看路线, 这是bug

                //设置当前所在地
                etStartAddress.setText(locationAddress);
//                etStartAddress.setEnabled(false); //禁用输入

                //设置起点
                mStartPoint = convertToLatLonPoint(new LatLng(latitude, longitude)); //创建一个点需要经过两个步骤
                //获取城市区号
                city = aMapLocation.getCity();

                //停止定位后，本地定位服务并不会被销毁
                mLocationClient.stopLocation();

                //显示地图定位结果
                if (mListener != null) {
                    // 显示系统图标
                    mListener.onLocationChanged(aMapLocation);  //定义结果的回调即可实现地图的定位
                }

            } else {
                //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mLocationClient == null) {
            mLocationClient.startLocation();//启动定位
        }
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //销毁定位客户端，同时销毁本地定位服务。
        if (mLocationClient != null) {
            mLocationClient.onDestroy();
        }
        mapView.onDestroy();
    }
    /**
     * 点击地图
     */
    @Override
    public void onMapClick(LatLng latLng) {
        //终点
        mEndPoint = convertToLatLonPoint(latLng); //一点就有终点了

        //开始路线搜索
        startRouteSearch(); //点击一下就开始根据起始点搜索路线
    }

    /**
     * 初始化路线
     */
    private void initRoute() {
        try {
            routeSearch = new RouteSearch(this);
        } catch (AMapException e) {
            e.printStackTrace();
        }
        routeSearch.setRouteSearchListener((RouteSearch.OnRouteSearchListener) this); //设置路线寻找监听器
    }

    /**
     * 公交规划路径结果
     *
     * @param busRouteResult 结果
     * @param code           结果码
     */
    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int code) {
        aMap.clear();// 清理地图上的所有覆盖物
        if (code == AMapException.CODE_AMAP_SUCCESS) {
            if (busRouteResult != null && busRouteResult.getPaths() != null) {
                if (busRouteResult.getPaths().size() > 0) {
                    final BusPath busPath = busRouteResult.getPaths().get(0);
                    if (busPath == null) {
                        return;
                    }
                    BusRouteOverlay busRouteOverlay = new BusRouteOverlay(
                            this, aMap, busPath,
                            busRouteResult.getStartPos(),
                            busRouteResult.getTargetPos());
                    busRouteOverlay.removeFromMap();
                    busRouteOverlay.addToMap();
                    busRouteOverlay.zoomToSpan();

                    int dis = (int) busPath.getDistance();
                    int dur = (int) busPath.getDuration();
                    String des = MapUtil.getFriendlyTime(dur) + "(" + MapUtil.getFriendlyLength(dis) + ")";
//                    Log.d(TAG, des);

                    tvTime.setText(des);
                    bottomLayout.setVisibility(View.VISIBLE);
                    bottomLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(RouteActivity.this,
                                    RouteDetailActivity.class);
                            intent.putExtra("type",3);
                            intent.putExtra("path", busPath);
                            startActivity(intent);
                        }
                    });


                } else if (busRouteResult.getPaths() == null) {
                    showMsg("对不起，没有搜索到相关数据！");
                }
            } else {
                showMsg("对不起，没有搜索到相关数据！");
            }
        } else {
            showMsg("错误码；" + code);
        }
    }



    /**
     * 驾车规划路径结果
     *
     * @param driveRouteResult 结果
     * @param code            结果码
     */
    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int code) {
        aMap.clear();// 清理地图上的所有覆盖物
        if (code == AMapException.CODE_AMAP_SUCCESS) {
            if (driveRouteResult != null && driveRouteResult.getPaths() != null) {
                if (driveRouteResult.getPaths().size() > 0) {
                    final DrivePath drivePath = driveRouteResult.getPaths()
                            .get(0);
                    if(drivePath == null) {
                        return;
                    }
                    DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(
                            this, aMap, drivePath,
                            driveRouteResult.getStartPos(),
                            driveRouteResult.getTargetPos(), null);
                    drivingRouteOverlay.removeFromMap();
                    drivingRouteOverlay.addToMap();
                    drivingRouteOverlay.zoomToSpan();

                    int dis = (int) drivePath.getDistance();
                    int dur = (int) drivePath.getDuration();
                    String des = MapUtil.getFriendlyTime(dur)+"("+MapUtil.getFriendlyLength(dis)+")";
//                    Log.d(TAG, des);

                    tvTime.setText(des);
                    bottomLayout.setVisibility(View.VISIBLE);
                    bottomLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(RouteActivity.this,
                                    RouteDetailActivity.class);
                            intent.putExtra("type",2);
                            intent.putExtra("path", drivePath);
                            startActivity(intent);
                        }
                    });

                } else if (driveRouteResult.getPaths() == null) {
                    showMsg("对不起，没有搜索到相关数据！");
                }
            } else {
                showMsg("对不起，没有搜索到相关数据！");
            }
        } else {
            showMsg("错误码；" + code);
        }
    }


    /**
     * 骑行规划路径结果
     *
     * @param rideRouteResult 结果
     * @param code            结果码
     */
    @Override
    public void onRideRouteSearched(final RideRouteResult rideRouteResult, int code) {
        aMap.clear();// 清理地图上的所有覆盖物
        if (code == AMapException.CODE_AMAP_SUCCESS) {
            if (rideRouteResult != null && rideRouteResult.getPaths() != null) {
                if (rideRouteResult.getPaths().size() > 0) {
                    final RidePath ridePath = rideRouteResult.getPaths()
                            .get(0);
                    if(ridePath == null) {
                        return;
                    }
                    RideRouteOverlay rideRouteOverlay = new RideRouteOverlay(
                            this, aMap, ridePath,
                            rideRouteResult.getStartPos(), //起点
                            rideRouteResult.getTargetPos()); //终点
                    rideRouteOverlay.removeFromMap();
                    rideRouteOverlay.addToMap(); //不是自己类就是祖宗类的方法
                    rideRouteOverlay.zoomToSpan(); //不是自己类就是祖宗类的方法

                    int dis = (int) ridePath.getDistance();
                    int dur = (int) ridePath.getDuration();
                    String des = MapUtil.getFriendlyTime(dur)+"("+MapUtil.getFriendlyLength(dis)+")";
//                    Log.d(TAG, des);

                    tvTime.setText(des); //启动显示Intent
                    bottomLayout.setVisibility(View.VISIBLE);
                    bottomLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(RouteActivity.this,
                                    RouteDetailActivity.class);
                            intent.putExtra("type",1);
                            intent.putExtra("path", ridePath);
                            startActivity(intent);
                        }
                    });


                } else if (rideRouteResult.getPaths() == null) {
                    showMsg("对不起，没有搜索到相关数据！");
                }
            } else {
                showMsg("对不起，没有搜索到相关数据！");
            }
        } else {
            showMsg("错误码；" + code);
        }
    }



    /**
     * 开始路线搜索
     */
    private void startRouteSearch() {
        //在地图上添加起点Marker
        aMap.addMarker(new MarkerOptions()
                .position(convertToLatLng(mStartPoint))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
        //在地图上添加终点Marker
        aMap.addMarker(new MarkerOptions()
                .position(convertToLatLng(mEndPoint))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.end)));

        //搜索路线 构建路径的起终点
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                mStartPoint, mEndPoint);
//        //构建步行路线搜索对象
//        RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo, RouteSearch.WalkDefault);
//        // 异步路径规划步行模式查询
//        routeSearch.calculateWalkRouteAsyn(query); //等待回调返回结果
        //出行方式判断
        switch (TRAVEL_MODE) {
            case 0://步行
                //构建步行路线搜索对象
                RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo, RouteSearch.WalkDefault);
                // 异步路径规划步行模式查询
                routeSearch.calculateWalkRouteAsyn(query);
                break;
            case 1://骑行
                //构建骑行路线搜索对象
                RouteSearch.RideRouteQuery rideQuery = new RouteSearch.RideRouteQuery(fromAndTo, RouteSearch.WalkDefault);
                //骑行规划路径计算
                routeSearch.calculateRideRouteAsyn(rideQuery);
                break;
            case 2://驾车
                //构建驾车路线搜索对象  剩余三个参数分别是：途经点、避让区域、避让道路
                RouteSearch.DriveRouteQuery driveQuery = new RouteSearch.DriveRouteQuery(fromAndTo, RouteSearch.WalkDefault, null, null, "");
                //驾车规划路径计算
                routeSearch.calculateDriveRouteAsyn(driveQuery);
                break;
            case 3://公交
                //构建驾车路线搜索对象 第三个参数表示公交查询城市区号，第四个参数表示是否计算夜班车，0表示不计算,1表示计算
                RouteSearch.BusRouteQuery busQuery = new RouteSearch.BusRouteQuery(fromAndTo, RouteSearch.BusLeaseWalk, city,0);
                //公交规划路径计算
                routeSearch.calculateBusRouteAsyn(busQuery);
                break;
            default:
                break;
        }

    }

    /**
     * 步行规划路径结果
     *
     * @param walkRouteResult 结果
     * @param code            结果码
     */
    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int code) {
        aMap.clear();// 清理地图上的所有覆盖物
        if (code == AMapException.CODE_AMAP_SUCCESS) {
            if (walkRouteResult != null && walkRouteResult.getPaths() != null) { //walkRouteResult结果和结果中的path都要有值
                if (walkRouteResult.getPaths().size() > 0) { //walkRouteResult的path要大于0
                    final WalkPath walkPath = walkRouteResult.getPaths().get(0); //第一条path
                    if (walkPath == null) {
                        return;
                    }
                    //绘制路线
                    WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(
                            this, aMap, walkPath,
                            walkRouteResult.getStartPos(),
                            walkRouteResult.getTargetPos());
                    walkRouteOverlay.removeFromMap();
                    walkRouteOverlay.addToMap(); //WalkRouteOverlay类的addToMap, 将工具类中的walkPath添加到mMap中
                    walkRouteOverlay.zoomToSpan(); //WalkRouteOverlay类的zoomToSpan,拿到Bounds用在地图上

                    int dis = (int) walkPath.getDistance(); //距离
                    int dur = (int) walkPath.getDuration(); //时长
                    String des = MapUtil.getFriendlyTime(dur) + "(" + MapUtil.getFriendlyLength(dis) + ")"; //单位换算
//                    Log.d(TAG, des);

                    //显示步行花费时间
                    tvTime.setText(des);
                    bottomLayout.setVisibility(View.VISIBLE);
                    //跳转到路线详情页面
                    bottomLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(RouteActivity.this,
                                    RouteDetailActivity.class);
                            intent.putExtra("type",0);
                            intent.putExtra("path", walkPath);
                            startActivity(intent);
                        }
                    });

                } else if (walkRouteResult.getPaths() == null) {
                    showMsg("对不起，没有搜索到相关数据！");
                }
            } else {
                showMsg("对不起，没有搜索到相关数据！");
            }
        } else {
            showMsg("错误码；" + code);
        }
    }

    private void showMsg(String msg){
        Toast.makeText(this,msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 初始化出行方式
     */
    private void initTravelMode() {
        Spinner spinner = findViewById(R.id.spinner);
        bottomLayout = findViewById(R.id.bottom_layout);
        tvTime = findViewById(R.id.tv_time);
        etStartAddress = findViewById(R.id.et_start_address);

        etEndAddress = findViewById(R.id.et_end_address);
        //键盘按键监听
        etStartAddress.setOnKeyListener((View.OnKeyListener) this);
        etEndAddress.setOnKeyListener((View.OnKeyListener) this);


        //将可选内容与ArrayAdapter连接起来
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, travelModeArray);
        //设置下拉列表的风格
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        spinner.setAdapter(arrayAdapter);
        //添加事件Spinner事件监听
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TRAVEL_MODE = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) { //只能是先终点后起点(先有终点才能弄两边的路线)
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
            //获取输入框的值 出发地（起点）
            String startAddress = etStartAddress.getText().toString().trim();
            //获取输入框的值 目的地（终点）
            String endAddress = etEndAddress.getText().toString().trim();

            //判断出发地是否有值  不管这个值是定位还是手动输入
            if (startAddress.isEmpty()) {
                showMsg("请输入当前的出发地");
                return false;
            }
            //判断目的地是否有值
            if (endAddress.isEmpty()) {
                showMsg("请输入要前往的目的地");
                return false;
            }

            //当出发地输入框有值的时候，判断这个值是否是定位的地址，是则说明你没有更改过，则不需要进行地址转坐标，不是则需要转换。
            if (!locationAddress.equals(startAddress)) {
                tag = 1;
                GeocodeQuery startQuery = new GeocodeQuery(startAddress, city);
                geocodeSearch.getFromLocationNameAsyn(startQuery);
            } else {
                tag = -1;
            }

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            //隐藏软键盘
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);

            //通过输入的目的地转为经纬度，然后进行地图上添加标点，最后计算出行路线规划

            // name表示地址，第二个参数表示查询城市，中文或者中文全拼，citycode、adcode
            GeocodeQuery endQuery = new GeocodeQuery(endAddress, city);
            geocodeSearch.getFromLocationNameAsyn(endQuery);
            return true;
        }
        return false;
    }


    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {

    }

    /**
     * 地址转坐标
     *
     * @param geocodeResult
     * @param rCode
     */
    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int rCode) {
        if (rCode == PARSE_SUCCESS_CODE) {
            List<GeocodeAddress> geocodeAddressList = geocodeResult.getGeocodeAddressList();
            if (geocodeAddressList != null && geocodeAddressList.size() > 0) {
                //判断是不是起点的搜索
                if (tag == 1) {
                    //起点
                    mStartPoint = geocodeAddressList.get(0).getLatLonPoint();
                } else {
                    //终点
                    mEndPoint = geocodeAddressList.get(0).getLatLonPoint();
                }

                if (mStartPoint != null && mEndPoint != null) {
                    //开始路线搜索
                    startRouteSearch();
                }
            }
        } else {
            showMsg("获取坐标失败");
        }
    }

}

