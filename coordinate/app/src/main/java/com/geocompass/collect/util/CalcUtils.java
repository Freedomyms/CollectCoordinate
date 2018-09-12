package com.geocompass.collect.util;

import android.icu.math.BigDecimal;

/**
 * Created by admin on 2018/7/21.
 */

public class CalcUtils {

    public static double decimalControl(double input, int bt) {
//        StringBuffer format = new StringBuffer("#.");
//        if(bt <= 0)
//                return input;
//        for (int i=1; i<=bt; i++)
//            format.append("#");
//
//        DecimalFormat df = new DecimalFormat(format.toString());
//        String reslut = df.format(input);
//        return Double.parseDouble(reslut);

        if (bt <= 0)
            return input;

        double result;
        try {
            BigDecimal b = new BigDecimal(input);
            result = b.setScale(bt, BigDecimal.ROUND_HALF_UP).doubleValue();
            return result;
        } catch (Exception e) {
            result = input;
            return result;
        }

    }

}
