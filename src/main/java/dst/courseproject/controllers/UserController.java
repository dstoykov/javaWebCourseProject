package dst.courseproject.controllers;

import dst.courseproject.exceptions.InvalidReCaptchaException;
import dst.courseproject.exceptions.PasswordsMismatchException;
import dst.courseproject.exceptions.UserAlreadyExistsException;
import dst.courseproject.models.binding.UserRegisterBindingModel;
import dst.courseproject.models.binding.UserEditBindingModel;
import dst.courseproject.models.service.UserServiceModel;
import dst.courseproject.models.view.UserViewModel;
import dst.courseproject.models.view.VideoViewModel;
import dst.courseproject.recaptcha.ReCaptchaService;
import dst.courseproject.services.UserService;
import dst.courseproject.services.VideoService;
import dst.courseproject.util.UserUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Base64;
import java.util.Set;

@Controller
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final VideoService videoService;
    private final ReCaptchaService reCaptchaService;
    private final ModelMapper modelMapper;

    @Autowired
    public UserController(UserService userService, VideoService videoService, ReCaptchaService reCaptchaService, ModelMapper modelMapper) {
        this.userService = userService;
        this.videoService = videoService;
        this.reCaptchaService = reCaptchaService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/register")
    public ModelAndView register(ModelAndView modelAndView, Model model, Principal principal) {
        if (principal != null) {
            return new ModelAndView("redirect:/");
        }
        modelAndView.setViewName("register");
        modelAndView.addObject("title", "Register");
        if (!model.containsAttribute("registerInput")) {
            model.addAttribute("registerInput", new UserRegisterBindingModel());
        }
        if (model.containsAttribute("passwordError")) {
            modelAndView.addObject("passwordError");
        }
        if (model.containsAttribute("userExistsError")) {
            modelAndView.addObject("userExistsError");
        }
        if (model.containsAttribute("invalidCaptchaError")) {
            modelAndView.addObject("invalidCaptchaError");
        }

        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView register(@Valid @ModelAttribute(name = "registerInput") UserRegisterBindingModel userBindingModel, BindingResult bindingResult, ModelAndView modelAndView, RedirectAttributes redirectAttributes, @RequestParam("g-recaptcha-response") String gCaptchaResponse) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.registerInput", bindingResult);
            redirectAttributes.addFlashAttribute("registerInput", userBindingModel);
            modelAndView.setViewName("redirect:register");
        } else {
            try {
                this.reCaptchaService.captcha(gCaptchaResponse);
                this.userService.register(userBindingModel);
                modelAndView.setViewName("redirect:login");
                redirectAttributes.addFlashAttribute("success", "Successfully registered");
            } catch (PasswordsMismatchException e) {
                redirectAttributes.addFlashAttribute("passwordError", "error");
                redirectAttributes.addFlashAttribute("registerInput", userBindingModel);
                modelAndView.setViewName("redirect:register");
            } catch (UserAlreadyExistsException e) {
                redirectAttributes.addFlashAttribute("userExistsError", "error");
                modelAndView.setViewName("redirect:register");
            } catch (InvalidReCaptchaException e) {
                redirectAttributes.addFlashAttribute("invalidCaptchaError", "error");
                redirectAttributes.addFlashAttribute("registerInput", userBindingModel);
                modelAndView.setViewName("redirect:register");
            }
        }

        return modelAndView;
    }

    @GetMapping("/login")
    public ModelAndView login(ModelAndView modelAndView, Model model, Principal principal) {
        if (principal != null) {
            return new ModelAndView("redirect:/");
        }
        modelAndView.setViewName("login");
        modelAndView.addObject("title", "Login");
        if (model.containsAttribute("success")) {
            modelAndView.addObject("success");
        }

        return modelAndView;
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView profile(ModelAndView modelAndView) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserViewModel userViewModel = this.userService.getUserViewModelByEmail(userDetails.getUsername());
        Boolean isAdmin = UserUtils.hasRole("ADMIN", userViewModel.getAuthorities());
        Boolean isModerator = UserUtils.hasRole("MODERATOR", userViewModel.getAuthorities());

        Set<VideoViewModel> videoViewModels = this.videoService.getVideosByUserAsViewModels(this.modelMapper.map(userViewModel, UserServiceModel.class));

        modelAndView.setViewName("user-profile");
        modelAndView.addObject("user", userViewModel);
        modelAndView.addObject("isAdmin", isAdmin);
        modelAndView.addObject("isModerator", isModerator);
        modelAndView.addObject("videos", videoViewModels);
        modelAndView.addObject("title", "My Profile");

        return modelAndView;
    }

    @GetMapping("/profile/edit")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView editProfile(ModelAndView modelAndView, Model model, ModelMapper mapper, Principal principal) {
        UserServiceModel userServiceModel = this.userService.getUserServiceModelByEmail(principal.getName());
        modelAndView.setViewName("user-edit");
        modelAndView.addObject("title", "Edit Profile");

        if (!model.containsAttribute("userInput")) {
            UserEditBindingModel userEditBindingModel = mapper.map(userServiceModel, UserEditBindingModel.class);
            model.addAttribute("userInput", userEditBindingModel);
        }
        if (model.containsAttribute("passwordError")) {
            modelAndView.addObject("passwordError");
        }

        return modelAndView;
    }

    @PostMapping("/profile/edit")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView editProfile(@Valid @ModelAttribute(name = "userInput") UserEditBindingModel userEditBindingModel, BindingResult bindingResult, ModelAndView modelAndView, RedirectAttributes redirectAttributes, Principal principal) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userInput", bindingResult);
            redirectAttributes.addFlashAttribute("userInput", userEditBindingModel);
            modelAndView.setViewName("redirect:/users/profile/edit");
        } else {
            try {
                this.userService.editUserDataByEmail(userEditBindingModel, principal.getName());
                modelAndView.setViewName("redirect:../profile");
            } catch (PasswordsMismatchException e) {
                redirectAttributes.addFlashAttribute("userInput", userEditBindingModel);
                redirectAttributes.addFlashAttribute("passwordError", "error");
                modelAndView.setViewName("redirect:/users/profile/edit");
            }
        }

        return modelAndView;
    }

    @GetMapping("/{email}")
    public ModelAndView viewOtherProfile(@PathVariable("email") String encodedEmail, ModelAndView modelAndView, Principal principal) {
        String email = new String(Base64.getDecoder().decode(encodedEmail.getBytes()));
        if (principal != null) {
            UserServiceModel target = this.userService.getUserServiceModelByEmail(email);
            if (this.userService.isUserModeratorByEmail(principal.getName())) {
                modelAndView.setViewName("redirect:../admin/users/" + target.getId());
            } else if (principal.getName().equals(email)) {
                modelAndView.setViewName("redirect:profile");
            } else {
                this.processDataForOthersProfile(email, modelAndView);
            }
        } else {
            this.processDataForOthersProfile(email, modelAndView);
        }

        return modelAndView;
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView logout(@RequestParam(required = false, name = "logout") String logout, ModelAndView modelAndView, RedirectAttributes redirectAttributes) {
        modelAndView.setViewName("redirect:/login");

        if (logout != null) {
            redirectAttributes.addFlashAttribute("logout", logout);
        }

        return modelAndView;
    }

    private void processDataForOthersProfile(String email, ModelAndView modelAndView) {
        UserViewModel userViewModel = this.userService.getUserViewModelByEmail(email);
        Set<VideoViewModel> videoViewModels = this.videoService.getVideosByUserAsViewModels(this.modelMapper.map(userViewModel, UserServiceModel.class));
        Boolean isAdmin = UserUtils.hasRole("ADMIN", userViewModel.getAuthorities());
        Boolean isModerator = UserUtils.hasRole("MODERATOR", userViewModel.getAuthorities());

        modelAndView.setViewName("user-other-user-profile");
        modelAndView.addObject("user", userViewModel);
        modelAndView.addObject("videos", videoViewModels);
        modelAndView.addObject("isAdminUser", isAdmin);
        modelAndView.addObject("isModeratorUser", isModerator);
        modelAndView.addObject("title", UserUtils.getUserFullName(this.modelMapper.map(userViewModel, UserServiceModel.class)));
    }
}
