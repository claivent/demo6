package com.claivent.demo6;


import com.claivent.demo6.model.User;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UserController {
  private final UserServices userServices;

  public UserController(UserServices userServices) {
    this.userServices = userServices;
  }


  @GetMapping("/users")
    public String users (Model model)   {
        model.addAttribute("users", userServices.getUsers());
        return "users"; // → templates/users.html
}

@CrossOrigin(origins = "http://localhost:4200")
  @GetMapping("/api/users")
  @ResponseBody
  public List<User> usersApi(){
    return userServices.getUsers();
}



}
