package lmc.web;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@ControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ModelAndView handleOptimisticLock(OptimisticLockingFailureException ex,
                                             RedirectAttributes ra,
                                             HttpServletRequest request) {

        log.warn("Optimistic lock detected; user={} referer={}", request.getRemoteUser(), request.getHeader("Referer"), ex);

        ra.addFlashAttribute("errorMessage", "Конфигурацията беше променена от друг потребител. Направените промени няма да бъдат запазени!");
        return new ModelAndView("redirect:/home");
    }
}
