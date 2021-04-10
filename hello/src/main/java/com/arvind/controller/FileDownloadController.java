package com.arvind.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FileDownloadController {
//	@PostMapping(value="/getuploadedtransactions", params = "action=download")
//	public String downloadAccount(@ModelAttribute(value="acctName") String acctName, ModelMap model) {
//		Map<String, Object> modelMap = uploadService.fetchUploadedTransactions(acctName);
//		model.addAllAttributes(modelMap);
//		return (String) modelMap.get("view");
//	}

	/*
    @RequestMapping(value = "downloadFile", method = RequestMethod.GET)
    public StreamingResponseBody getSteamingFile(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"demo.pdf\"");
        InputStream inputStream = new FileInputStream(new File("C:\\demo-file.pdf"));
        return outputStream -> {
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                System.out.println("Writing some bytes..");
                outputStream.write(data, 0, nRead);
            }
        };
    }
	 */
	
	// @RequestMapping(value = "/download", method = RequestMethod.GET) 
	// public ResponseEntity<Object> downloadFile() throws IOException {
	@PostMapping(value="/getuploadedtransactionstest", params = "action=download")
	public ResponseEntity<Object> downloadAccount(@ModelAttribute(value="acctName") String acctName, ModelMap model) throws IOException {

		// String filename = "/var/tmp/mysql.png";
		String filename = "C:\\tmp\\Transactions.txt";
		File file = new File(filename);
		InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
		HttpHeaders headers = new HttpHeaders();

		headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getName()));
		headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
		headers.add("Pragma", "no-cache");
		headers.add("Expires", "0");

		ResponseEntity<Object> responseEntity = ResponseEntity.ok().headers(headers).contentLength(file.length())
				.contentType(MediaType.parseMediaType("application/txt")).body(resource);

		return responseEntity;
	}
}
