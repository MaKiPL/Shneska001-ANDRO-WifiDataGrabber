package maki.sneshka_01snifferdataminer;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.sql.Struct;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActv extends AppCompatActivity {

    private View button;
    private WifiManager wm;
    private LocationManager lm;
    private SharedPreferences sp;
    private java.lang.String[] st;
    private List<String> stList;
    private LocationListener lss;
    private static final int ACCESS_WIFI_STATE = 527;
    private static final int CHANGE_WIFI_STATE = 914;
    private boolean bPr = false;
    private boolean bIsInCollection = true;
    private String _lastLoc;
    private String _newLoc;
    private boolean bBanContinue = true;

    class Router
    {
        public String BSSID;
        public String SSID;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_actv);
        sp = getSharedPreferences("spm", Activity.MODE_PRIVATE);
        wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        registerReceiver(brr, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        lss = new ls();
        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, CHANGE_WIFI_STATE, 0);
        Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(loc!=null)
        _lastLoc = loc.getLatitude() + "//" + loc.getLongitude();
        else _lastLoc = "0.000//0.000";
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0,lss);
        bPr = true;
    }

    public void ShneskaStart(View v)
    {
        InitData();
        Toast.makeText(this, "Starting...", Toast.LENGTH_SHORT);
        wm.startScan();
        Toast.makeText(this, "MEOW!", Toast.LENGTH_LONG);
    }

    public void CountWEP(View v)
    {
        InitData();
        int wepcount = 0;
        //Pattern p = Pattern.compile("[WEP]");
        for(int i = 0; i<st.length; i++)
        {
            //Matcher m = p.matcher(st[i]);
            //if(m.matches())
                //wepcount++;
            if(contains(st[i], "[WEP]"))
                wepcount++;
            /*if(st[i].contains("[WEP]"))
                wepcount++;*/
        }
        TextView wepcountlbl = (TextView) findViewById(R.id.textView5);
        wepcountlbl.setText("WEPs: " + Integer.toString(wepcount));
    }

    public static boolean contains(String str, String trgt) {
        for (int i = 0; i < str.length(); i++)
            for (int j = 0, k = 0; i+k < str.length() && str.charAt(i + k) == trgt.charAt(j); j++, k++)
                if (j == trgt.length()-1)
                    return true;
        return false;
    }

    BroadcastReceiver brr = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
            {
                List<ScanResult> lrs =  wm.getScanResults();
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, lss);

                if(!_lastLoc.equals(_newLoc) && _newLoc != null)
                    _lastLoc = _newLoc;

                if(lrs.size() == 0) return;
                for(int i = 0; i<lrs.size(); i++)
                {
                    if(st.length != 0)
                    {
                        bIsInCollection = false;
                        for(int n = 0; n<st.length; n++)
                        {
                            String tempbss = st[n].substring(0,17);
                            if(tempbss.equals(lrs.get(i).BSSID))
                                bIsInCollection = true;
                        }
                        if(!bIsInCollection)
                        {
                            checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, CHANGE_WIFI_STATE, 0);
                            String ret = sp.getString("meow", "");
                            if(_lastLoc == null)
                                _lastLoc = "0.000/0.000";
                            String pc = "///;" + lrs.get(i).BSSID + "//" + lrs.get(i).SSID + "//" + lrs.get(i).capabilities + _lastLoc; //FIRST ENTRY
                            ret += pc;
                            TextView lbl4 = (TextView) findViewById(R.id.textView4);
                            lbl4.setText(pc);
                            UpdateData(ret);
                        }
                    }
                    else
                    {
                        String ret = sp.getString("meow", "");
                        String pc = lrs.get(i).BSSID + "//" + lrs.get(i).SSID + "//" + lrs.get(i).capabilities + _lastLoc; //FIRST ENTRY
                        ret += pc;
                        TextView lbl4 = (TextView) findViewById(R.id.textView4);
                        lbl4.setText(pc);
                        UpdateData(ret);
                    }
                }
            }
        }
    };

    private class ls implements LocationListener {
        @Override
        public void onLocationChanged(Location location)
        {
            _newLoc = "//" + Double.toString(location.getLatitude()) + "/" + Double.toString(location.getLongitude());
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

    private void InitData()
    {
        String ret = sp.getString("meow", "");
        if(ret != "")
            st = ret.split("///;");
        else st = new String[0];

        if(bPr)
        {
            TextView lbl = (TextView)findViewById(R.id.textView2);
            int lng = st.length;
            lbl.setText(Integer.toString(lng));
            ListView lw = (ListView) findViewById(R.id.listb);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, st);
            lw.setAdapter(adapter);
        }

    }

    private void UpdateData(String ret)
    {
        SharedPreferences.Editor ed = sp.edit();
        ed.putString("meow", ret);
        ed.commit();
        InitData();
    }

    private void InsertNewEntry(String s)
    {

    }
}
