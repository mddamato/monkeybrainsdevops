package com.jabil;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/")
public class HomeController {

	private RecordRepository recordRepository;
	
	@Autowired
	public HomeController(RecordRepository repository){
		this.recordRepository = repository;
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public String home(ModelMap model){
		List<Record> records = recordRepository.findAll();
		model.addAttribute("records", records);
		model.addAttribute("insertRecord", new Record());
		return "home";
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public String insertData(ModelMap model, @ModelAttribute("insertRecord") @Valid Record record, BindingResult result){
		if(!result.hasErrors()){
			recordRepository.save(record);
		}
		return home(model);
	}
}
