package com.tytogroup.mobitrack;

import android.app.Application;
import android.content.Context;
import android.telephony.TelephonyManager;

import com.parse.Parse;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "8FCIjEmzBAO2B0plVWzVA5pQbIb0ZX4tSBNBed17", "wMwdgvLnlHbw3NHHMOIcSDyEkgYrTzMAzaqmqw2p");
    }

    public static String GetCountryCode(TelephonyManager manager,Context context){
        String CountryID="";
        String CountryZipCode="";

        //getNetworkCountryIso
        CountryID= manager.getSimCountryIso().toUpperCase();
        String[] rl=context.getResources().getStringArray(R.array.CountryCodes);
        for(int i=0;i<rl.length;i++){
            String[] g=rl[i].split(",");
            if(g[1].trim().equals(CountryID.trim())){
                CountryZipCode=g[0];
                break;
            }
        }

        return CountryZipCode;
    }
}
