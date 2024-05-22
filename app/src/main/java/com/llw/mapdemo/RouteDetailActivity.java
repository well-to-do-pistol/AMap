package com.llw.mapdemo;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.RidePath;
import com.amap.api.services.route.WalkPath;
import com.llw.mapdemo.adapter.BusSegmentListAdapter;
import com.llw.mapdemo.adapter.DriveSegmentListAdapter;
import com.llw.mapdemo.adapter.RideSegmentListAdapter;
import com.llw.mapdemo.adapter.WalkSegmentListAdapter;
import com.llw.mapdemo.util.MapUtil;
import com.llw.mapdemo.util.SchemeBusStep;

import java.util.ArrayList;
import java.util.List;

public class RouteDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvTitle, tvTime;
    private RecyclerView rv;//列表


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_route_detail);
        //初始化
        initView();
    }

    /**
     * 初始化
     */
    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        tvTitle = findViewById(R.id.tv_title);
        tvTime = findViewById(R.id.tv_time);
        rv = findViewById(R.id.rv);
        //高亮状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
//        组合这些标志（“SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | SYSTEM_UI_FLAG_LIGHT_STATUS_BAR”）会产生一个 UI，您的应用程序将在其中布局其内容以覆盖整个屏幕（包括状态栏后面），同时保持可见的浅色主题状态栏。 此设置通常用于创建身临其境的布局，使用户仍然可以清晰地看到设备状态，特别是在屏幕顶部具有浅色方案的应用程序中特别有用
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); //点击Navigation图标退出
            }
        });
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        switch (intent.getIntExtra("type", 0)) {
            case 0://步行
                walkDetail(intent);
                break;
            case 1://骑行
                rideDetail(intent);
                break;
            case 2://驾车
                driveDetail(intent);
                break;
            case 3://公交
                busDetail(intent);
                break;
            default:
                break;
        }
    }
    /**
     * 步行详情
     * @param intent
     */
    private void walkDetail(Intent intent) {
        tvTitle.setText("步行路线规划");
        WalkPath walkPath = intent.getParcelableExtra("path"); //拿到walkpath
        String dur = MapUtil.getFriendlyTime((int) walkPath.getDuration());
        String dis = MapUtil.getFriendlyLength((int) walkPath.getDistance());
        tvTime.setText(dur + "(" + dis + ")");
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new WalkSegmentListAdapter(R.layout.item_segment, walkPath.getSteps()));
    }

    /**
     * 骑行详情
     * @param intent
     */
    private void rideDetail(Intent intent) {
        tvTitle.setText("骑行路线规划");
        RidePath ridePath = intent.getParcelableExtra("path");
        String dur = MapUtil.getFriendlyTime((int) ridePath.getDuration());
        String dis = MapUtil.getFriendlyLength((int) ridePath.getDistance());
        tvTime.setText(dur + "(" + dis + ")");
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new RideSegmentListAdapter(R.layout.item_segment, ridePath.getSteps()));
    }

    /**
     * 驾车详情
     * @param intent
     */
    private void driveDetail(Intent intent) {
        tvTitle.setText("驾车路线规划");
        DrivePath drivePath = intent.getParcelableExtra("path");
        String dur = MapUtil.getFriendlyTime((int) drivePath.getDuration());
        String dis = MapUtil.getFriendlyLength((int) drivePath.getDistance());
        tvTime.setText(dur + "(" + dis + ")");
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new DriveSegmentListAdapter(R.layout.item_segment, drivePath.getSteps()));
    }

    /**
     * 公交方案数据组装
     * @param list
     * @return
     */
    private List<SchemeBusStep> getBusSteps(List<BusStep> list) {
        List<SchemeBusStep> busStepList = new ArrayList<>();
        SchemeBusStep start = new SchemeBusStep(null);
        start.setStart(true);
        busStepList.add(start);
        for (BusStep busStep : list) {
            if (busStep.getWalk() != null && busStep.getWalk().getDistance() > 0) {
                SchemeBusStep walk = new SchemeBusStep(busStep);
                walk.setWalk(true);
                busStepList.add(walk);
            }
            if (busStep.getBusLine() != null) {
                SchemeBusStep bus = new SchemeBusStep(busStep);
                bus.setBus(true);
                busStepList.add(bus);
            }
            if (busStep.getRailway() != null) {
                SchemeBusStep railway = new SchemeBusStep(busStep);
                railway.setRailway(true);
                busStepList.add(railway);
            }

            if (busStep.getTaxi() != null) {
                SchemeBusStep taxi = new SchemeBusStep(busStep);
                taxi.setTaxi(true);
                busStepList.add(taxi);
            }
        }
        SchemeBusStep end = new SchemeBusStep(null);
        end.setEnd(true);
        busStepList.add(end);
        return busStepList;
    }
    /**
     * 公交详情
     * @param intent
     */
    private void busDetail(Intent intent) {
        tvTitle.setText("公交路线规划");
        BusPath busPath = intent.getParcelableExtra("path");
        String dur = MapUtil.getFriendlyTime((int) busPath.getDuration());
        String dis = MapUtil.getFriendlyLength((int) busPath.getDistance());
        tvTime.setText(dur + "(" + dis + ")");
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new BusSegmentListAdapter(R.layout.item_segment, getBusSteps(busPath.getSteps())));
    }


}