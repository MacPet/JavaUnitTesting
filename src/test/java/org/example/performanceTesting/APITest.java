package org.example.performanceTesting;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.newAccountRegistry;
import org.example.springboot.AccountController;
import org.example.springboot.Application;
import org.example.springboot.Transfer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class)
public class APITest {



    @Autowired
    private AccountController accountController;

    ObjectMapper mapper = new ObjectMapper();

    double TRANSFER_AMOUNT = 1.00;
    final int LOOP_SIZE = 10;

    private MockMvc mockMvc;

    public String createRequestJSON(String pesel, int i, double saldo){
        return
                "{ " + "\"pesel\": \"" + pesel + "\", " +
                        "\"name\": \"Name" + i + "\", " +
                        "\"surname\": \"Surname" + i + "\", " +
                        "\"balance\":  \"" + saldo  + "\", " +
                        "\"history\": [] " +  "}";
    }

    private String createTransferRequestJSON(Transfer transfer)  {
try{
    return mapper.writeValueAsString(transfer);
}
catch (Exception e) {
    throw new RuntimeException("Error when JSONifying transfer: ", e);
    }


    }

    private double extractBalanceFromResponse(String responseBody) {

        try {
            JsonNode rootNode = mapper.readTree(responseBody);
            return rootNode.get("balance").asDouble();
        } catch (Exception e) {
            throw new RuntimeException("Error when extracting balance from response:", e);
        }
    }


    @BeforeEach
    void setUp(){
        newAccountRegistry.clear();
        this.mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();
    }

    @Test
    public void APICreateAndDeleteMustTakeLessThan500ms() throws Exception {

        final long timeLimit = 500L;


        for (int i = 1; i <= LOOP_SIZE; i++) {


            String pesel = String.format("%011d", i);
            String requestBody = createRequestJSON(pesel, i, 0.00);



            long startTest = System.nanoTime();

            this.mockMvc.perform(post("/accounts")
                            .contentType("application/json")
                            .content(requestBody))
                    .andExpect(status().is(HttpStatus.OK.value()));

            this.mockMvc.perform(delete("/accounts/" + pesel))
                    .andExpect(status().is(HttpStatus.OK.value()));

            long endTest = System.nanoTime();

            assertThat((endTest - startTest) / 1_000_000).isLessThan(timeLimit);
        }
    }


    @Test
    public void APIMoneyTransferMustTakeLessThan500ms() throws Exception {

        final long timeLimit = 500L;

        String pesel = String.format("%011d", 1);

        String requestBody = createRequestJSON(pesel, 1, 0);

        this.mockMvc.perform(post("/accounts")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().is(HttpStatus.OK.value()));


        for (int i = 1; i <= LOOP_SIZE; i++) {

            System.out.println(i);
            Transfer transfer = new Transfer("incoming", TRANSFER_AMOUNT);
            String transferRequestBody = createTransferRequestJSON(transfer);

            long startTest = System.nanoTime();



            this.mockMvc.perform(post("/accounts/" + pesel + "/transfer")
                            .contentType("application/json")
                            .content(transferRequestBody))
                    .andExpect(status().is(HttpStatus.OK.value()));

            long endTest = System.nanoTime();

            assertThat((endTest - startTest) / 1_000_000).isLessThan(timeLimit);
        }


        MvcResult result = this.mockMvc.perform(get("/accounts/" + pesel))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andReturn();


        String responseBody = result.getResponse().getContentAsString();
        double finalSaldo = extractBalanceFromResponse(responseBody);

        assertThat(finalSaldo).isEqualTo(TRANSFER_AMOUNT * LOOP_SIZE);
    }








}
