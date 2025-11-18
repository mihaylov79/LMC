package lmc.web;

import lmc.offer.model.Offer;
import lmc.offer.service.OfferService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/offers")
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @GetMapping("/details/{offerId}")
    public ModelAndView showOfferDetails(@PathVariable UUID offerId){
        Offer offer = offerService.getOfferWithConfiguration(offerId);

        ModelAndView modelAndView = new ModelAndView("offer-detalis");
        modelAndView.addObject("offer", offer);

        return modelAndView;

    }
}
