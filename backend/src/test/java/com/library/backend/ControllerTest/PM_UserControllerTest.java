package com.library.backend.ControllerTest;

import com.library.backend.entity.PM_User;
import com.library.backend.repository.PM_AdminRepository;
import com.library.backend.repository.PM_UserRepository;
import com.library.backend.utils.JwtUtil;
import com.library.backend.controller.PM_UserController;
import com.library.backend.service.JwtService;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.DigestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import org.json.JSONObject;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class PM_UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    //@MockBean
    //private JwtService jwtService;

    /**
     * .perform() : 执行一个MockMvcRequestBuilders的请求；MockMvcRequestBuilders有.get()、.post()、.put()、.delete()等请求。
     * .andDo() : 添加一个MockMvcResultHandlers结果处理器,可以用于打印结果输出(MockMvcResultHandlers.print())。
     * .andExpect : 添加MockMvcResultMatchers验证规则，验证执行结果是否正确。
     */
    @Test
    public void login() throws Exception {

        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": 21808081, \"password\": 8081}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(content);

        String userToken = jsonObject.getString("token");
        System.out.print(userToken);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": 12345, \"password\": 54321}"))
                .andDo(print())
                .andExpect(status().is(401));
    }

    @Test
    public void adminLogin() throws Exception {
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": 21808080, \"password\": 8080}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(content);

        String adminToken = jsonObject.getString("token");
        System.out.print(adminToken);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": 12345, \"password\": 54321}"))
                .andDo(print())
                .andExpect(status().is(401));
    }

    @Test
    public void register() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"21808081\", \"password\": \"8081\"}"))
                .andDo(print())
                .andExpect(status().is(409));

        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": 21808080, \"password\": 8080}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(content);

        String adminToken = jsonObject.getString("token");
        System.out.print(adminToken);

        this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/21909090")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer "+adminToken));

        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"21909090\", \"password\": \"9090\"}"))
                .andDo(print())
                .andExpect(status().is(201));

        this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/21909090")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+adminToken))
                .andDo(print())
                .andExpect(status().is(204));
    }

    @Test
    public void adminAddUser() throws Exception {
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": 21808080, \"password\": 8080}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(content);

        String adminToken = jsonObject.getString("token");
        System.out.print(adminToken);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"21808081\", \"password\": 8081}")
                        .header("Authorization", "Bearer "+adminToken))
                .andDo(print())
                .andExpect(status().is(409));

        this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/21909090")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer "+adminToken));

        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"21909090\", \"password\": 9090}")
                        .header("Authorization", "Bearer "+adminToken))
                .andDo(print())
                .andExpect(status().is(201));

        this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/21909090")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+adminToken))
                .andDo(print())
                .andExpect(status().is(204));
    }

    @Test
    public void getUserInfo() throws Exception {
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": 21808080, \"password\": 8080}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(content);

        String adminToken = jsonObject.getString("token");
        System.out.print(adminToken);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/api//users/21808081")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+adminToken))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void updateUserInfo() throws Exception {
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": 21808080, \"password\": 8080}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(content);

        String adminToken = jsonObject.getString("token");
        System.out.print(adminToken);

        this.mockMvc.perform(MockMvcRequestBuilders.patch("/api/users/21808081")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+adminToken)
                        .content("{\"name\": \"chen\", \"email\": \"chen@fudan.edu.cn\", \"phone\": \"15185199778\", \"address\": \"fudan\"}"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void updateUserPassword() throws Exception {
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": 21808080, \"password\": 8080}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(content);

        String adminToken = jsonObject.getString("token");
        System.out.print(adminToken);

        this.mockMvc.perform(MockMvcRequestBuilders.patch("/api/users/21808081/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+adminToken)
                        .content("{\"oldPassword\": \"8081\", \"newPassword\": \"8081\"}"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void getUserList() throws Exception {
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": 21808080, \"password\": 8080}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(content);

        String adminToken = jsonObject.getString("token");
        System.out.print(adminToken);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+adminToken))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void resetUserPassword() throws Exception {
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": 21808080, \"password\": 8080}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(content);

        String adminToken = jsonObject.getString("token");
        System.out.print(adminToken);

        this.mockMvc.perform(MockMvcRequestBuilders.patch("/api/admin/user/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+adminToken)
                        .content("{\"id\": 21808081, \"newPassword\": \"8080\"}"))
                .andDo(print())
                .andExpect(status().isOk());

        this.mockMvc.perform(MockMvcRequestBuilders.patch("/api/admin/user/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+adminToken)
                        .content("{\"id\": 21808081, \"newPassword\": \"8081\"}"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void deleteUserById() throws Exception {
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": 21808080, \"password\": 8080}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(content);

        String adminToken = jsonObject.getString("token");
        System.out.print(adminToken);

        this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/21909090")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer "+adminToken));

        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"21909090\", \"password\": 9090}")
                        .header("Authorization", "Bearer "+adminToken))
                .andDo(print())
                .andExpect(status().is(201));

        this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/21909090")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+adminToken))
                .andDo(print())
                .andExpect(status().is(204));
    }
}
