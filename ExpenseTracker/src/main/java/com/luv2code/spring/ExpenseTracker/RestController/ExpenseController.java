package com.luv2code.spring.ExpenseTracker.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

//import com.luv2code.spring.ExpenseTracker.Model.Category;
import com.luv2code.spring.ExpenseTracker.Model.Expense;
import com.luv2code.spring.ExpenseTracker.Model.Income;
import com.luv2code.spring.ExpenseTracker.Model.User;
import com.luv2code.spring.ExpenseTracker.Service.ExpenseService;
import com.luv2code.spring.ExpenseTracker.Service.IncomeService;
import com.luv2code.spring.ExpenseTracker.Service.UserService;

@Controller
public class ExpenseController {


	@Autowired
	private UserService userService;
	
	@Autowired
	private ExpenseService expenseService;
	
	@Autowired
	private IncomeService incomeService;
	
	//List expenses for user
	@GetMapping("/expenses")
	public String findAllExpesnes(Model theModel) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user= userService.findByUserName(auth.getName()).get(0);
		List<Expense> expenses = userService.findByUserId(user.getId());
		int sumAmount=0;
		for(Expense sum:expenses) {
			sumAmount+=sum.getAmount();
		}
		List<Income> incomes = incomeService.findByUserId(user.getId());
		int sumIncome=0;
		for(Income sum:incomes) {
			sumIncome+=sum.getAmount();
		}
		int balance = sumIncome-sumAmount;
		
		String res="";
		if(balance>0) {
			 res="Your Remaining Balance is"+" Rs "+" "+balance;
		}
		else {
			res="You have exhausted your Income by"+" Rs"+" "+Math.abs(balance);
		}
		theModel.addAttribute("res", res);
		theModel.addAttribute("totalAmount", sumAmount);
		theModel.addAttribute("Expenses", expenses);
		return "list-expenses";
	}
	
	/*//list expense by users
	@GetMapping("/expenseByUserId/{id}")
	public String findExpesnesByUserId(@PathVariable("userid") int userid,Model theModel) {
		List<Expense> expenses = userService.findByUserId(userid);
		theModel.addAttribute("ExpensesByUser", expenses);
		return "list-expenses-User";
	}*/
	@GetMapping("/expenseById/{expenseId}")
	public String findExpesnesByUserId(Model theModel,@PathVariable("expenseId") int id) {
		Expense expense = expenseService.findById(id);
		User user=expense.getUser();
		int theId = user.getId();
		List<Expense> expenses = userService.findByUserId(theId);
		theModel.addAttribute("ExpensesByUser", expenses);
		return "list-expenses-User";
	}
	//Add expenses
	@GetMapping("/showFormForAdd")
	public String showFormForAdd(Model theModel) {
		
		// create model attribute to bind form data
		Expense theExpense = new Expense();
		
		//List<Category> theCategory = userService.findAllCategory();

		
		theModel.addAttribute("expense", theExpense);
		//theModel.addAttribute("category", theCategory);
		
		return "add-expense";
	}
	
	//save expense
	@PostMapping("/save")
	public String saveExpense(@ModelAttribute("expense") Expense theExpenses) {
		
		int flag=0;
		List<String> lunch = new ArrayList<String>();
		lunch.add("lunch");
		lunch.add("food");
		lunch.add("breakfast");
		lunch.add("dinner");
		
		List<String> entertainment = new ArrayList<>();
		entertainment.add("movie");
		entertainment.add("game");
		entertainment.add("cricket");
		entertainment.add("indoor");
		entertainment.add("football");
		entertainment.add("sports");
		
		List<String>utilities =new ArrayList<>();
		utilities.add("bill");
		utilities.add("electricity");
		utilities.add("petrol");
		utilities.add("bike");
		utilities.add("car");
		
		List<String>emi = new ArrayList<String>();
		emi.add("emi");
		emi.add("loan");
		
		
		// save the User
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user= userService.findByUserName(auth.getName()).get(0);
		theExpenses.setUser(user);
		theExpenses.setExpenseAddedDate(new java.sql.Date(new Date().getTime()));
		String notes=theExpenses.getExpenseNotes();
		//Category category=new Category();
		String[] listNotes = notes.split(" ");
		for(int i=0;i<listNotes.length;i++) {
			if(lunch.contains(listNotes[i].toLowerCase())) {
				flag=1;
				break;
			}
			else if(entertainment.contains(listNotes[i].toLowerCase())) {
				flag=2;
				break;
			}
			else if(emi.contains(listNotes[i].toLowerCase())) {
				flag=3;
				break;
			}
			else if(utilities.contains(listNotes[i].toLowerCase())){
				flag=4;
				break;
			}
		}
		if(flag==1) {
			theExpenses.setCategory("FOOD");
		}
		else if(flag==2) {
			theExpenses.setCategory("ENTERTAINMENT");
		}
		else if(flag==3) {
			theExpenses.setCategory("EMI");
		}
		else if(flag==4) {
			theExpenses.setCategory("UTILITIES");
		}
		else {
			theExpenses.setCategory("others");
		}
		//theExpenses.setCategory(category);
		userService.saveExpense(theExpenses);
		
		// use a redirect to prevent duplicate submissions
		return "redirect:/expenses";
	}
		
	//Update expense
	@GetMapping("/showFormForUpdate/{expenseId}")
	public String showFormForUpdate( @PathVariable("expenseId") int theExpenses,
									Model theModel) {
		
		// get the employee from the service
		Expense theExpense =expenseService.findById(theExpenses);
		
		// set employee as a model attribute to pre-populate the form
		theModel.addAttribute("expense", theExpense);
		//List<Category> theCategory = userService.findAllCategory();
		//theModel.addAttribute("category", theCategory);
		// send over to our form
		return "add-expense";			
	}
	
	//Delete expense
	@GetMapping("/delete/{expenseId}")
	public String delete( @PathVariable("expenseId") int theExpenses,
									Model theModel) {
		expenseService.deleteById(theExpenses);
		return "redirect:/expenses";
	}
}
