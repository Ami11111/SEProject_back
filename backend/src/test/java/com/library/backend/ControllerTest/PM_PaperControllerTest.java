package com.library.backend.ControllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.backend.config.TestSecurityConfig;
import com.library.backend.controller.PM_PaperController;
import com.library.backend.dto.PaperDTO;
import com.library.backend.entity.*;
import com.library.backend.repository.PM_AdminRepository;
import com.library.backend.repository.PM_PaperAdditionalRepository;
import com.library.backend.repository.PM_PaperRepository;
import com.library.backend.repository.PM_UserRepository;
import com.library.backend.service.JwtService;
import com.library.backend.service.MyUserDetailsService;
import com.library.backend.service.PaperService;
import com.library.backend.utils.JwtRequestFilter;
import com.library.backend.utils.JwtUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.parameters.P;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.Base64;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// WebMvcTest仅加载web相关配置（如controller），其他需要mockbean
@WebMvcTest(controllers = PM_PaperController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtRequestFilter.class // 排除真实的jwt过滤器
        ))
@Import(TestSecurityConfig.class) // 导入自定义测试配置，目的是跳过jwt过滤器
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc // 自动配置MockMvc
public class PM_PaperControllerTest {
    @Autowired
    private MockMvc mockMvc;

    // papercontroller定义的都要mockbean
    @MockBean
    private PM_PaperRepository paperRepository;
    @MockBean
    private PM_UserRepository userRepository;
    @MockBean
    private PM_AdminRepository adminRepository;
    @MockBean
    private PM_PaperAdditionalRepository paperAdditionalRepository;
    @MockBean
    private PaperService paperService;
    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private JwtService jwtService;

    @Test
    public void insertPaper() throws Exception {
        // 用户不存在
        String token = "validToken";
        String bearerToken = "Bearer " + token;
        PM_User user = new PM_User();
        user.setId(6060);
        BDDMockito.given(jwtUtil.extractUsername(token)).willReturn(String.valueOf(user.getId()));
        BDDMockito.given(userRepository.findById(user.getId())).willReturn(null);
        mockMvc.perform( // perform发起请求
                        MockMvcRequestBuilders.post("/api/papers") // 构造请求
                                .header("Authorization", bearerToken) // http header
                                .contentType(MediaType.APPLICATION_JSON) // 前端发送来的数据格式
                                .content("{}") // 发送来的数据
                                .accept(MediaType.APPLICATION_JSON) // 后端返回去的数据格式
                ).andDo(print()) // 其他操作，比如输出到控制台
                .andExpect(status().is(401)); // 断言。这里判断返回状态

        // 用户没有维护姓名信息
        BDDMockito.given(userRepository.findById(user.getId())).willReturn(user);
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/papers")
                                .header("Authorization", bearerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(401));

        // 论文status非法
        PaperDTO paperDTO = new PaperDTO();
        paperDTO.setDOI("doi.abc");
        paperDTO.setStatus("approve");
        ObjectMapper objectMapper = new ObjectMapper();
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/papers")
                                .header("Authorization", bearerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(paperDTO))
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(401));

        // 用户不是论文作者
        user.setName("ming");
        paperDTO.setStatus("notSubmit");
        BDDMockito.given(paperService.getSeq(paperDTO, user.getName())).willReturn(null);
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/papers")
                                .header("Authorization", bearerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(paperDTO))
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(401));

        // 成功
        String seq = "first";
        PM_Paper paper = new PM_Paper();
        ArrayList<PM_PaperAdditional> paperAdditionals = new ArrayList<>();
        PM_AuthorPaper authorPaper = new PM_AuthorPaper();
        authorPaper.setAuthorId(user.getId());
        authorPaper.setPaperId(paperDTO.getDOI());
        authorPaper.setSeq(PM_AuthorPaper.Seq.valueOf(seq));
        BDDMockito.given(paperService.getSeq(paperDTO, user.getName())).willReturn(seq);
        BDDMockito.given(paperService.paperDTOToPaper(paperDTO)).willReturn(paper);
        BDDMockito.given(paperService.paperDTOToPaperAdditionals(paperDTO)).willReturn(paperAdditionals);
        BDDMockito.doNothing().when(paperService).insertPaper(paper, paperAdditionals, authorPaper);
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/papers")
                                .header("Authorization", bearerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(paperDTO))
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(200));
    }

    @Test
    public void deletePaper() throws Exception {
        // 非管理员
        String token = "validToken";
        String bearerToken = "Bearer " + token;
        PM_Admin admin = new PM_Admin();
        admin.setId(8080);
        String doi = "doi.abc";
        String doi64 = Base64.getEncoder().encodeToString(doi.getBytes());
        BDDMockito.given(jwtUtil.extractUsername(token)).willReturn(String.valueOf(admin.getId()));
        BDDMockito.given(adminRepository.findById(admin.getId())).willReturn(null);
        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/papers/{doi}", doi64)
                                .header("Authorization", bearerToken)
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(401));

        // 论文不存在
        BDDMockito.given(adminRepository.findById(admin.getId())).willReturn(admin);
        BDDMockito.given(paperRepository.findByDoi(doi)).willReturn(null);
        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/papers/{doi}", doi64)
                                .header("Authorization", bearerToken)
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(404));

        // 成功
        PM_Paper paper=new PM_Paper();
        paper.setDoi(doi);
        BDDMockito.given(paperRepository.findByDoi(doi)).willReturn(paper);
        BDDMockito.doNothing().when(paperRepository).deleteByDoi(doi);
        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/papers/{doi}", doi64)
                                .header("Authorization", bearerToken)
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(204));

    }

//        String content =result.getResponse().getContentAsString(); // 获取返回响应
//        JSONObject jsonObject=new JSONObject(content);
//        String token=jsonObject.getString("token");


}
