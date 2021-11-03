package com.selenium.common.linkedin.util;

import com.selenium.common.linkedin.BO.BOPost;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

public class LinkedinExcel {

    private static Properties properties;

    public static void generateExcel(ArrayList<BOPost> xResults) {
        Workbook workbook = null;
        try {
            init();
            workbook = new XSSFWorkbook();

            createHeaderExcel(workbook);

            addResultsExcel(workbook, xResults);

            afterEditExcel(workbook);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            try {
                if (workbook != null) {
                    workbook.close();
                }
            } catch (Exception i) {
            }
        }
    }

    private static void init() throws Exception{
        properties = new Properties();
        properties.load(new FileInputStream(new File("src/main/resources/application.properties")));

    }

    private static void createCell(Row header, int cell, String value, CellStyle headerStyle) {
        Cell headerCell = header.createCell(cell);
        headerCell.setCellValue(value);
        headerCell.setCellStyle(headerStyle);
    }

    private static void createHeaderExcel(Workbook workbook) {

        Integer add = 0;
        Boolean excelFormatForBrowser = Boolean.parseBoolean((String) properties.get("excelFormatForBrowser"));
        if(excelFormatForBrowser){
            //validar que para un excel se ve bien asi
            add = 1;
        }
        Sheet sheet = workbook.createSheet("Jobs");
        sheet.setColumnWidth(0, 1 + (add * 2500));
        sheet.setColumnWidth(1, 2 + (add * 7000));
        sheet.setColumnWidth(2, 2 + (add * 7000));
        sheet.setColumnWidth(3, 4 + (add * 18000));
        sheet.setColumnWidth(4, 8 + (add * 35000));

        Row header = sheet.createRow(0);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.AUTOMATIC.getIndex());

        XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        font.setFontHeightInPoints((short) 14);
        font.setColor(XSSFFont.COLOR_NORMAL);
        font.setBold(true);
        headerStyle.setFont(font);

        createCell(header, 0, "Score:", headerStyle);
        createCell(header, 1, "People:", headerStyle);
        createCell(header, 2, "State:", headerStyle);
        createCell(header, 3, "Profile:", headerStyle);
        createCell(header, 4, "Post:", headerStyle);
    }

    private static void addResultsExcel(Workbook workbook, ArrayList<BOPost> xResults) {
        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);

        Sheet sheet = workbook.getSheetAt(0);
        for (int i = 0; i < xResults.toArray().length; i++) {

            Row row = sheet.createRow(2 + i);
            BOPost xBOPost = xResults.get(i);

            createCell(row, 0, xBOPost.getScore().toString(), style);
            createCell(row, 1, xBOPost.getPerfilName(), style);
            createCell(row, 2, xBOPost.getIsConnect(), style);
            createCell(row, 3, xBOPost.getUrl(), style);
            createCell(row, 4, xBOPost.getText(), style);
        }
    }

    private static void afterEditExcel(Workbook workbook) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date date = new Date();
        String result = "Results - " + formatter.format(date) + "-" + uuid() +".xlsx";
        String fileLocation = (String) properties.get("fileLocation");
        FileOutputStream outputStream = new FileOutputStream( fileLocation + result);
        workbook.write(outputStream);
        workbook.close();
    }

    private static String uuid(){
        String result = java.util.UUID.randomUUID().toString();
        result = result.replaceAll("-", "").substring(0,10);

        return result;
    }
}
