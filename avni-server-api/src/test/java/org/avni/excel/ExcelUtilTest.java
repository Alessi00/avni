package org.avni.excel;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class ExcelUtilTest {
    @Test
    public void getDateFromString() {
        ExcelUtil.getDateFromString("20/Jan/2018");
        ExcelUtil.getDateFromString("2014-Feb-13");
        ExcelUtil.getDateFromString("2012-10-27");
        ExcelUtil.getDateFromString("2012-10-27 00:00:00");
        ExcelUtil.getDateFromString("2012-10-27 00:00:00.0");
        ExcelUtil.getDateFromString("2012-10-27 10:30:00.0");
        System.out.println(ExcelUtil.getDateFromString("2012-12-27"));
    }
}
