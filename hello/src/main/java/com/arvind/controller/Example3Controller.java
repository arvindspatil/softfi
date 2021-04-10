package com.arvind.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.arvind.util.MediaTypeUtils;

@Controller
public class Example3Controller {
	private static final String DIRECTORY = "C:/tmp";
	private static final String DEFAULT_FILE_NAME = "Pay.xlsx";

	@Autowired
	private ServletContext servletContext;

	@GetMapping("/download3")
	public void downloadFile3(HttpServletResponse resonse,
			@RequestParam(defaultValue = DEFAULT_FILE_NAME) String fileName) throws IOException {

		MediaType mediaType = MediaTypeUtils.getMediaTypeForFileName(this.servletContext, fileName);
		System.out.println("fileName: " + fileName);
		System.out.println("mediaType: " + mediaType);

		File file = new File(DIRECTORY + "/" + fileName);

		// Content-Type
		// application/pdf
		resonse.setContentType(mediaType.getType());

		// Content-Disposition
		resonse.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName());

		// Content-Length
		resonse.setContentLength((int) file.length());

		BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(file));
		BufferedOutputStream outStream = new BufferedOutputStream(resonse.getOutputStream());

		byte[] buffer = new byte[1024];
		int bytesRead = 0;
		while ((bytesRead = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, bytesRead);
		}
		outStream.flush();
		inStream.close();
	}

	@GetMapping("/download4")
	public void downloadFile4(HttpServletResponse resonse,
			@RequestParam(defaultValue = DEFAULT_FILE_NAME) String fileName) throws IOException {

		MediaType mediaType = MediaTypeUtils.getMediaTypeForFileName(this.servletContext, fileName);
		System.out.println("fileName: " + fileName);
		System.out.println("mediaType: " + mediaType);

		File file = new File(DIRECTORY + "/" + fileName);

		// Content-Type
		// application/pdf
		resonse.setContentType(mediaType.getType());

		// Content-Disposition
		resonse.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName());

		// Content-Length
// arvind		resonse.setContentLength((int) file.length());
		Workbook workbook = new XSSFWorkbook();
		
		Sheet sheet = workbook.createSheet("Persons");
		sheet.setColumnWidth(0, 6000);
		sheet.setColumnWidth(1, 4000);
		
		Row header = sheet.createRow(0);
		
		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		
		XSSFFont font = ((XSSFWorkbook) workbook).createFont();
		font.setFontName("Arial");
		font.setFontHeightInPoints((short) 11);
		// font.setColor(XSSFColor.from(color, map));
		// font.setColor(new XSSFColor( Color.decode("#7CFC00")));
		font.setColor(IndexedColors.WHITE.index);
		font.setBold(true);
		headerStyle.setFont(font);
		
		Cell headerCell = header.createCell(0);
		headerCell.setCellValue("Name");
		headerCell.setCellStyle(headerStyle);
		
		headerCell = header.createCell(1);
		headerCell.setCellValue("Age");
		headerCell.setCellStyle(headerStyle);
		
		CellStyle style = workbook.createCellStyle();
		style.setWrapText(true);
		
		Row row = sheet.createRow(2);
		Cell cell = row.createCell(0);
		cell.setCellValue("John Smith");
		cell.setCellStyle(style);
		
		cell = row.createCell(1);
		cell.setCellValue(20);
		cell.setCellStyle(style);

		ServletOutputStream outputStream = resonse.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
         
        outputStream.close();
        
        
//		BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(file));
//		BufferedOutputStream outStream = new BufferedOutputStream(resonse.getOutputStream());

//		byte[] buffer = new byte[1024];
//		int bytesRead = 0;
//		while ((bytesRead = inStream.read(buffer)) != -1) {
//			outStream.write(buffer, 0, bytesRead);
//		}
//		outStream.flush();
//		inStream.close();
	}

}




//Workbook workbook = new XSSFWorkbook();
//
//Sheet sheet = workbook.createSheet("Persons");
//sheet.setColumnWidth(0, 6000);
//sheet.setColumnWidth(1, 4000);
//
//Row header = sheet.createRow(0);
//
//CellStyle headerStyle = workbook.createCellStyle();
//headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
//headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//
//XSSFFont font = ((XSSFWorkbook) workbook).createFont();
//font.setFontName("Arial");
//font.setFontHeightInPoints((short) 16);
//font.setBold(true);
//headerStyle.setFont(font);
//
//Cell headerCell = header.createCell(0);
//headerCell.setCellValue("Name");
//headerCell.setCellStyle(headerStyle);
//
//headerCell = header.createCell(1);
//headerCell.setCellValue("Age");
//headerCell.setCellStyle(headerStyle);
//
//CellStyle style = workbook.createCellStyle();
//style.setWrapText(true);
//
//Row row = sheet.createRow(2);
//Cell cell = row.createCell(0);
//cell.setCellValue("John Smith");
//cell.setCellStyle(style);
//
//cell = row.createCell(1);
//cell.setCellValue(20);
//cell.setCellStyle(style);
//
//File currDir = new File(".");
//String path = currDir.getAbsolutePath();
//String fileLocation = path.substring(0, path.length() - 1) + "temp.xlsx";
//
//FileOutputStream outputStream;
//try {
//	outputStream = new FileOutputStream(fileLocation);
//	workbook.write(outputStream);
//	workbook.close();
//} catch (IOException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//}
