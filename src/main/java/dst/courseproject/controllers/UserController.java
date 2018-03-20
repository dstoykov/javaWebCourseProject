package dst.courseproject.controllers;

import dst.courseproject.models.binding.RegisterUserBindingModel;
import dst.courseproject.models.binding.UserLoginBindingModel;
import dst.courseproject.models.service.UserServiceModel;
import dst.courseproject.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/users")
public class UserController {
    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public ModelAndView register(ModelAndView modelAndView) {
        modelAndView.setViewName("register");
        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView registerConfirm(ModelAndView modelAndView, RegisterUserBindingModel userBindingModel) {
        if (!userBindingModel.getPassword().equals(userBindingModel.getConfirmPassword())) {
            modelAndView.setViewName("redirect:login");
        } else {
            this.userService.createUser(userBindingModel);
            modelAndView.setViewName("redirect:login");
        }
        return modelAndView;
    }

    @GetMapping("login")
    public ModelAndView login(ModelAndView modelAndView) {
        modelAndView.setViewName("login");
        return modelAndView;
    }

    @PostMapping("login")
    public ModelAndView loginConfirm(ModelAndView modelAndView, UserLoginBindingModel loginBindingModel) {
        UserServiceModel userByEmail = this.userService.getUserByEmail(loginBindingModel.getEmail());
        if (userByEmail != null) {
            modelAndView.addObject("username", userByEmail.getFirstName());
            modelAndView.setViewName("redirect:/");
        }

        return modelAndView;
    }
}
