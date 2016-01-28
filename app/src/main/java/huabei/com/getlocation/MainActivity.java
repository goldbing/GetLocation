package huabei.com.getlocation;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;


public class MainActivity extends Activity {
    private Button start_GPS;
    private TextView show_GPS;
    private LocationManager myLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start_GPS = (Button) findViewById(R.id.start_GPS);
        show_GPS = (TextView) findViewById(R.id.show_GPS);
        //开启监听
        start_GPS.setOnClickListener(btnClickListener);
    }

    //mLocationManager.removeUpdates()是停止当前的GPS位置监听
    public Button.OnClickListener btnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button btn = (Button) v;
            if (btn.getId() == R.id.start_GPS) {
                if (!gpsIsOpen()) {
                    openGPS(MainActivity.this);
                }
                //获取位置信息
                getLocation();
            }
        }
    };

    //判断当前是否开启了GPS
    private boolean gpsIsOpen() {
        boolean isOpen = true;
        LocationManager alm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (!alm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {//没有开启GPS
            isOpen = false;
        }
        return isOpen;
    }

    @Override
    protected void onDestroy() {
        myLocationManager.removeUpdates(locationListener);
        super.onDestroy();
    }

    private void getLocation() {
        //获取位置管理服务
        myLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        //查找服务信息
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE); //定位精度: 最高
        criteria.setAltitudeRequired(false); //海拔信息：不需要
        criteria.setBearingRequired(false); //方位信息: 不需要
        criteria.setCostAllowed(true);  //是否允许付费
        criteria.setPowerRequirement(Criteria.POWER_LOW); //耗电量: 低功耗
        String provider = myLocationManager.getBestProvider(criteria, true); //获取GPS信息
        myLocationManager.requestLocationUpdates(provider, 2000, 5, locationListener);
    }

    //监听GPS位置改变后得到新的经纬度
    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub
            if (location != null) {
                //获取国家，省份，城市的名称
                String str = getAddress(location).toString();
                String country = str.substring(str.indexOf("countryName=") + 12, str.indexOf(",hasLatitude"));
                String admin = str.substring(str.indexOf("admin=") + 6, str.indexOf(",sub-admin"));
                String city = str.substring(str.indexOf("locality=") + 9, str.indexOf(",thoroughfare"));
                show_GPS.setText(city+"======="+str);
            } else {
                show_GPS.setText("获取不到数据");
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

    };

    // 获取地址信息
    private List<Address> getAddress(Location location) {
        List<Address> result = null;
        try {
            if (location != null) {
                Geocoder gc = new Geocoder(this, Locale.getDefault());
                result = gc.getFromLocation(location.getLatitude(),
                        location.getLongitude(), 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public static final void openGPS(Context context) {
        Intent GPSIntent = new Intent();
        GPSIntent.setClassName("com.android.settings",
                "com.android.settings.widget.SettingsAppWidgetProvider");
        GPSIntent.addCategory("android.intent.category.ALTERNATIVE");
        GPSIntent.setData(Uri.parse("custom:3"));
        try {
            PendingIntent.getBroadcast(context, 0, GPSIntent, 0).send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }
}
