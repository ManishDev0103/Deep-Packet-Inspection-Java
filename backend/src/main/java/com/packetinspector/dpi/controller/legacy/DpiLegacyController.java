package com.packetinspector.dpi.controller.legacy;

import com.packetinspector.dpi.model.entity.AppType;
import com.packetinspector.dpi.repository.ConnectionRepository;
import com.packetinspector.dpi.service.RuleManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

@Controller
@RequestMapping("/legacy")
public class DpiLegacyController {

    private final RuleManager ruleManager;
    private final ConnectionRepository connectionRepository;

    public DpiLegacyController(RuleManager ruleManager, ConnectionRepository connectionRepository) {
        this.ruleManager = ruleManager;
        this.connectionRepository = connectionRepository;
    }

    @GetMapping("/report")
    public String showReport(Model model) {
        model.addAttribute("ruleStats", ruleManager.getStats());
        model.addAttribute("connections", connectionRepository.findTop100ByOrderByIdDesc());
        return "report";
    }

    @GetMapping("/rules")
    public String showRules(Model model) {
        model.addAttribute("ruleStats", ruleManager.getStats());
        return "rules";
    }

    @PostMapping("/rules/add")
    public String addRule(@RequestParam("ruleType") String ruleType,
                          @RequestParam("ruleValue") String ruleValue,
                          RedirectAttributes ra) {
        try {
            String type = ruleType.trim().toUpperCase(Locale.ROOT);
            String val = ruleValue.trim();
            switch (type) {
                case "IP" -> ruleManager.blockIp(val);
                case "APP" -> ruleManager.blockApp(AppType.fromDisplayName(val));
                case "DOMAIN" -> ruleManager.blockDomain(val);
                case "PORT" -> ruleManager.blockPort(Integer.parseInt(val));
                default -> throw new IllegalArgumentException("Unknown rule type: " + type);
            }
            ra.addFlashAttribute("successMsg", "Rule added: " + ruleType + " = " + ruleValue);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Failed to add rule: " + e.getMessage());
        }
        return "redirect:/legacy/rules";
    }

    @PostMapping("/rules/delete")
    public String deleteRule(@RequestParam("ruleType") String ruleType,
                             @RequestParam("ruleValue") String ruleValue,
                             RedirectAttributes ra) {
        try {
            String type = ruleType.trim().toUpperCase(Locale.ROOT);
            String val = ruleValue.trim();
            switch (type) {
                case "IP" -> ruleManager.unblockIp(val);
                case "APP" -> ruleManager.unblockApp(AppType.fromDisplayName(val));
                case "DOMAIN" -> ruleManager.unblockDomain(val);
                case "PORT" -> ruleManager.unblockPort(Integer.parseInt(val));
                default -> throw new IllegalArgumentException("Unknown rule type: " + type);
            }
            ra.addFlashAttribute("successMsg", "Rule deleted: " + ruleType + " = " + ruleValue);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Failed to delete rule: " + e.getMessage());
        }
        return "redirect:/legacy/rules";
    }
}
