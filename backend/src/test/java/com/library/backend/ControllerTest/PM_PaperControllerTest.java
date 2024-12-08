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
import com.library.backend.service.PaperService;
import com.library.backend.utils.JwtRequestFilter;
import com.library.backend.utils.JwtUtil;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
        // 非用户
        String token = "validToken";
        String bearerToken = "Bearer " + token;
        PM_User user = new PM_User();
        user.setId(6060);
        BDDMockito.given(jwtUtil.extractUsername(token)).willReturn(String.valueOf(user.getId()));
        BDDMockito.given(userRepository.findById(user.getId())).willReturn(null);
        MvcResult result = mockMvc.perform( // perform发起请求
                        MockMvcRequestBuilders.post("/api/papers") // 构造请求
                                .header("Authorization", bearerToken) // http header
                                .contentType(MediaType.APPLICATION_JSON) // 前端发送来的数据格式
                                .content("{}") // 发送来的数据
                                .accept(MediaType.APPLICATION_JSON) // 后端返回去的数据格式
                ).andDo(print()) // 其他操作，比如输出到控制台
                .andExpect(status().is(401)) // 断言。这里判断返回状态
                .andReturn(); // 返回MvcResult时必须有

        String content = result.getResponse().getContentAsString(); // 获取返回响应
        JSONObject jsonObject = new JSONObject(content);
        assertEquals("Unauthorized",jsonObject.get("message")); // 也已写在mockMvc里，见下一个


        // 用户没有维护姓名信息
        BDDMockito.given(userRepository.findById(user.getId())).willReturn(user);
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/papers")
                                .header("Authorization", bearerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(401))
                .andExpect(jsonPath("message").value("Unauthorized"));

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

        // 抛出异常
        BDDMockito.given(userRepository.findById(user.getId())).willThrow(new RuntimeException("Error"));
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/papers")
                                .header("Authorization", bearerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(paperDTO))
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(500))
                .andExpect(content().string("java.lang.RuntimeException: Error"));
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
        PM_Paper paper = new PM_Paper();
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

    @Test
    public void updatePaper() throws Exception {
        // 非用户或管理员
        String token = "validToken";
        String bearerToken = "Bearer " + token;
        String doi = "doi.abc";
        String doi64 = Base64.getEncoder().encodeToString(doi.getBytes());
        PM_User user = new PM_User();
        user.setId(6060);
        PM_Admin admin = new PM_Admin();
        admin.setId(8080);
        int id = 123;
        BDDMockito.given(jwtUtil.extractUsername(token)).willReturn(String.valueOf(id));
        BDDMockito.given(userRepository.findById(id)).willReturn(null);
        BDDMockito.given(adminRepository.findById(id)).willReturn(null);
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/papers/{doi}", doi64)
                                .header("Authorization", bearerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(401));

        // 论文不存在
        BDDMockito.given(adminRepository.findById(id)).willReturn(admin);
        BDDMockito.given(paperRepository.findByDoi(doi)).willReturn(null);
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/papers/{doi}", doi64)
                                .header("Authorization", bearerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(404));

        // 管理员修改成功
        PM_Paper paper = new PM_Paper();
        paper.setDoi(doi);
        PaperDTO paperDTO = new PaperDTO();
        paperDTO.setDOI(doi);
        paperDTO.setStatus("approve");
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayList<PM_PaperAdditional> paperAdditionals = new ArrayList<>();
        BDDMockito.given(paperRepository.findByDoi(doi)).willReturn(paper);
        BDDMockito.given(paperService.paperDTOToPaper(paperDTO)).willReturn(paper);
        BDDMockito.given(paperService.paperDTOToPaperAdditionals(paperDTO)).willReturn(paperAdditionals);
        BDDMockito.doNothing().when(paperService).insertPaper(paper, paperAdditionals);
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/papers/{doi}", doi64)
                                .header("Authorization", bearerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(paperDTO))
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(200));

        // 用户修改成功
        user.setName("ming");
        paperDTO.setStatus("review");
        String seq = "first";
        BDDMockito.given(adminRepository.findById(id)).willReturn(null);
        BDDMockito.given(userRepository.findById(id)).willReturn(user);
        BDDMockito.given(paperService.getSeq(paperDTO, user.getName())).willReturn(seq);
        BDDMockito.doNothing().when(paperService).insertPaper(eq(paper), eq(paperAdditionals), any(PM_AuthorPaper.class));
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/papers/{doi}", doi64)
                                .header("Authorization", bearerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(paperDTO))
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(200));

    }

    @Test
    public void uploadPaperFile() throws Exception {
        // 非用户
        String token = "validToken";
        String bearerToken = "Bearer " + token;
        String doi = "doi.abc";
        String doi64 = Base64.getEncoder().encodeToString(doi.getBytes());
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "text/plain", "content".getBytes());
        PM_User user = new PM_User();
        user.setId(6060);
        BDDMockito.given(jwtUtil.extractUsername(token)).willReturn(String.valueOf(user.getId()));
        BDDMockito.given(userRepository.findById(user.getId())).willReturn(null);
        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/papers/{doi}/file", doi64)
                                .file(file)
                                .header("Authorization", bearerToken)
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(401));

        // 论文不存在
        BDDMockito.given(userRepository.findById(user.getId())).willReturn(user);
        BDDMockito.given(paperRepository.findByDoi(doi)).willReturn(null);
        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/papers/{doi}/file", doi64)
                                .file(file)
                                .header("Authorization", bearerToken)
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(404));

        // 成功
        PM_Paper paper = new PM_Paper();
        paper.setDoi(doi);
        BDDMockito.given(paperRepository.findByDoi(doi)).willReturn(paper);
        BDDMockito.given(paperService.isAuthorOfPaper(any(), any(), any(), any())).willReturn(true);
        BDDMockito.doNothing().when(paperService).uploadFile(file, doi);
        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/papers/{doi}/file", doi64)
                                .file(file)
                                .header("Authorization", bearerToken)
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(200));
    }

    @Test
    public void findAll() throws Exception {
        // 非用户或管理员
        String token = "validToken";
        String bearerToken = "Bearer " + token;
        String doi = "doi.abc";
        String doi64 = Base64.getEncoder().encodeToString(doi.getBytes());
        PM_User user = new PM_User();
        user.setId(6060);
        BDDMockito.given(jwtUtil.extractUsername(token)).willReturn(String.valueOf(user.getId()));
        BDDMockito.given(userRepository.findById(user.getId())).willReturn(null);
        BDDMockito.given(jwtService.isAdmin(eq(token), any(HashMap.class))).willReturn(new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED));
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/papers")
                                .param("doi", doi64)
                                .param("id", String.valueOf(user.getId()))
                                .header("Authorization", bearerToken)
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(401));

        // 成功
        List<PM_Paper> papers = new ArrayList<>();
        BDDMockito.given(userRepository.findById(user.getId())).willReturn(user);
        BDDMockito.given(paperRepository.findPapersByUserIdAndDoi(user.getId(), doi)).willReturn(papers);
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/papers")
                                .param("doi", doi64)
                                .param("id", String.valueOf(user.getId()))
                                .header("Authorization", bearerToken)
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(200));

    }

    @Test
    public void downloadPaperFile() throws Exception {
        // 非用户
        String token = "validToken";
        String bearerToken = "Bearer " + token;
        String doi = "doi.abc";
        String doi64 = Base64.getEncoder().encodeToString(doi.getBytes());
        PM_User user = new PM_User();
        user.setId(6060);
        BDDMockito.given(jwtUtil.extractUsername(token)).willReturn(String.valueOf(user.getId()));
        BDDMockito.given(userRepository.findById(user.getId())).willReturn(null);
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/files/download/{doi}", doi64)
                                .header("Authorization", bearerToken)
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(401));

        // 论文不存在
        BDDMockito.given(userRepository.findById(user.getId())).willReturn(user);
        BDDMockito.given(paperRepository.findByDoi(doi)).willReturn(null);
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/files/download/{doi}", doi64)
                                .header("Authorization", bearerToken)
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(404));

        // 成功
        PM_Paper paper = new PM_Paper();
        paper.setDoi(doi);
        paper.setFileData("content".getBytes());
        BDDMockito.given(paperRepository.findByDoi(doi)).willReturn(paper);
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/files/download/{doi}", doi64)
                                .header("Authorization", bearerToken)
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(200))
                .andExpect(header().string("Content-Disposition","form-data; name=\"attachment\"; filename=\""+doi+"\""))
                .andExpect(header().string("Content-Type","application/octet-stream"));

    }

}
