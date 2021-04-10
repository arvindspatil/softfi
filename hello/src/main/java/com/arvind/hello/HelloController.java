package com.arvind.hello;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HelloController {

//	@Autowired
//	ActorDaoImpl actorDao;

	@Autowired
	ActorDao actorDao;

	@Autowired
	RestTemplate restTemplate;

	private static final Logger log = LoggerFactory.getLogger(HelloController.class);

//	@Bean
//	public RestTemplate restTemplate(RestTemplateBuilder builder) {
//		return builder.build();
//	}
//
//	@Bean
//	public CommandLineRunner run(RestTemplate restTemplate) throws Exception {
//		return args -> {
//			Quote quote = restTemplate.getForObject(
//					"https://gturnquist-quoters.cfapps.io/api/random", Quote.class);
//			log.info(quote.toString());
//		};
//	}

	@GetMapping ("/start")
	public String handleStart(Model model) {
		BookList books = new BookList();
	    List<Book> firstNames = actorDao.findEmployeeById(5);
	    for (Book book : firstNames) {
	    	books.addBook(book);
	    }
//	    for (int i = 1; i <= 3; i++) {
//	        books.addBook(new Book(i, String.valueOf(i), String.valueOf(i)));
//	    }
	    
//	    ObjectMapper mapper = new ObjectMapper();
//	    SimpleModule module = new SimpleModule();
//	    module.addDeserializer(Quote.class, new ItemDeserializer());
//	    mapper.registerModule(module);
//	    try {
//			Quote testQ = mapper.readValue(new URL(
//					"https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=IBM&outputsize=compact&apikey=LVOYR1B8IC22JABA"),
//					Quote.class);
//			log.info(testQ.toString());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	    
//			Quote quote = restTemplate.getForObject(
//					"https://gturnquist-quoters.cfapps.io/api/random", Quote.class);
//			log.info(quote.toString());
	    
	    model.addAttribute("form", books);
	    model.addAttribute("booktoupdate", new Book());
	    return "displayv6";
	}

	
	@GetMapping ("/hello")
	public String handleHello(Model model) {
		System.out.println("Hi Arvind");
		BookList books = new BookList();
	    List<Book> firstNames = actorDao.findEmployeeById(5);
	    for (Book book : firstNames) {
	    	books.addBook(book);
	    }	    	    
	    model.addAttribute("form", books);
//	    model.addAttribute("booktoupdate", new Book());
	    return "displayv5";
	}

	/*
	 * @GetMapping ("/hello/edit/{id}") public String handleEdit(Model model) {
	 * System.out.println("Hi Arvind"); BookList books = new BookList(); List<Book>
	 * firstNames = actorDao.findEmployeeById(5); for (Book book : firstNames) {
	 * books.addBook(book); } // for (int i = 1; i <= 3; i++) { // books.addBook(new
	 * Book(i, String.valueOf(i), String.valueOf(i))); // }
	 * 
	 * model.addAttribute("form", books); return "displayone"; }
	 * 
	 * 	@RequestMapping("/hello/edit/{id}")
	 * @RequestParam(value = "searchbox") String query, Model model
	 * @PathVariable(name = "id") int id
	 */	
	
	// @GetMapping(value = "/hello/edit")
	@RequestMapping(value="/hello/edit", method = RequestMethod.POST)
	public ModelAndView showEditProductPage(@ModelAttribute(value="booktoupdate") Book book) { // , Model model) {
	    ModelAndView mav = new ModelAndView("displayv6");
		BookList books = new BookList();
	    List<Book> firstNames = actorDao.findEmployeeByTitle(book.getTitle());
	    List<Book> allbooks = actorDao.findEmployeeById(100);
	    
	    for (Book bk : allbooks) {
	    	books.addBook(bk);
	    }	    	    
	    mav.addObject("form", books);
	    mav.addObject("result", firstNames);
	    return mav;
	}
}
