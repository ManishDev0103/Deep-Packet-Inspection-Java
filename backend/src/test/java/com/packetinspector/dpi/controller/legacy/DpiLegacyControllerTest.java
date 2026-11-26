package com.packetinspector.dpi.controller.legacy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class DpiLegacyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void showsReportView() throws Exception {
        mockMvc.perform(get("/legacy/report"))
                .andExpect(status().isOk())
                .andExpect(view().name("report"))
                .andExpect(model().attributeExists("ruleStats"))
                .andExpect(model().attributeExists("connections"));
    }

    @Test
    void showsRulesView() throws Exception {
        mockMvc.perform(get("/legacy/rules"))
                .andExpect(status().isOk())
                .andExpect(view().name("rules"))
                .andExpect(model().attributeExists("ruleStats"));
    }

    @Test
    void addsAndDeletesRuleViaLegacyForm() throws Exception {
        mockMvc.perform(post("/legacy/rules/add")
                        .param("ruleType", "IP")
                        .param("ruleValue", "192.168.1.100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/legacy/rules"))
                .andExpect(flash().attribute("successMsg", "Rule added: IP = 192.168.1.100"));

        mockMvc.perform(post("/legacy/rules/delete")
                        .param("ruleType", "IP")
                        .param("ruleValue", "192.168.1.100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/legacy/rules"))
                .andExpect(flash().attribute("successMsg", "Rule deleted: IP = 192.168.1.100"));
    }

    @Test
    void reportsConcreteFailureWhenRuleTypeIsUnknown() throws Exception {
        mockMvc.perform(post("/legacy/rules/add")
                        .param("ruleType", "UNKNOWN")
                        .param("ruleValue", "example"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/legacy/rules"))
                .andExpect(flash().attribute("errorMsg", "Failed to add rule: Unknown rule type: UNKNOWN"));
    }

    @Test
    void configuredNotFoundPathRendersCustomJspView() throws Exception {
        mockMvc.perform(get("/error/404.html"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));
    }
}
