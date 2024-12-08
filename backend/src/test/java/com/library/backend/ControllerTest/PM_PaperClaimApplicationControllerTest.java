package com.library.backend.ControllerTest;

import com.library.backend.config.TestSecurityConfig;
import com.library.backend.controller.PM_PaperClaimApplicationController;
import com.library.backend.entity.*;
import com.library.backend.repository.*;
import com.library.backend.service.PaperService;
import com.library.backend.utils.JwtRequestFilter;
import com.library.backend.utils.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PM_PaperClaimApplicationController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtRequestFilter.class))
@Import(TestSecurityConfig.class)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
class PM_PaperClaimApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PM_PaperRepository paperRepository;
    @MockBean
    private PM_UserRepository userRepository;
    @MockBean
    private PM_AdminRepository adminRepository;
    @MockBean
    private PM_AuthorPaperRepository authorPaperRepository;
    @MockBean
    private PM_AuthorPaperClaimRepository authorPaperClaimRepository;
    @MockBean
    private PaperService paperService;
    @MockBean
    private JwtUtil jwtUtil;

    @Test
    public void claimPaper() throws Exception {
        // 非用户
        String token = "validToken";
        String bearerToken = "Bearer " + token;
        PM_User user = new PM_User();
        user.setId(6060);
        String doi = "doi.abc";
        String doi64 = Base64.getEncoder().encodeToString(doi.getBytes());
        BDDMockito.given(jwtUtil.extractUsername(token)).willReturn(String.valueOf(user.getId()));
        BDDMockito.given(userRepository.findById(user.getId())).willReturn(null);
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/papers/request/claim")
                                .header("Authorization", bearerToken)
                                .param("doi", doi64)
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(401));

        // 用户没有姓名信息
        BDDMockito.given(userRepository.findById(user.getId())).willReturn(user);
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/papers/request/claim")
                                .header("Authorization", bearerToken)
                                .param("doi", doi64)
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(401));


        // 论文不存在
        user.setName("ming");
        BDDMockito.given(paperRepository.findByDoi(doi)).willReturn(null);
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/papers/request/claim")
                                .header("Authorization", bearerToken)
                                .param("doi", doi64)
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(404))
                .andExpect(jsonPath("message").value("Paper not found"));

        // 用户不是论文作者
        PM_Paper paper = new PM_Paper();
        paper.setDoi(doi);
        BDDMockito.given(paperRepository.findByDoi(doi)).willReturn(paper);
        BDDMockito.given(paperService.getSeq(paper, user.getName())).willReturn(null);
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/papers/request/claim")
                                .header("Authorization", bearerToken)
                                .param("doi", doi64)
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(401));

        // 成功
        PM_AuthorPaperClaim authorPaperClaim = new PM_AuthorPaperClaim();
        authorPaperClaim.setAuthorId(user.getId());
        authorPaperClaim.setPaperDoi(doi);
        BDDMockito.given(paperService.getSeq(paper, user.getName())).willReturn("first");
        BDDMockito.given(authorPaperClaimRepository.save(authorPaperClaim)).willReturn(authorPaperClaim);
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/papers/request/claim")
                                .header("Authorization", bearerToken)
                                .param("doi", doi64)
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(200))
                .andExpect(jsonPath("message").value("Claim applied successfully"));

    }

    @Test
    public void getClaim() throws Exception {
        // 非管理员
        String token = "validToken";
        String bearerToken = "Bearer " + token;
        PM_Admin admin = new PM_Admin();
        admin.setId(8080);
        BDDMockito.given(jwtUtil.extractUsername(token)).willReturn(String.valueOf(admin.getId()));
        BDDMockito.given(adminRepository.findById(admin.getId())).willReturn(null);
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/papers/request/claim")
                                .header("Authorization", bearerToken)
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(401));

        // 成功
        List<PM_AuthorPaperClaim> paperClaims = new ArrayList<>();
        PM_AuthorPaperClaim authorPaperClaim1 = new PM_AuthorPaperClaim();
        authorPaperClaim1.setAuthorId(123);
        authorPaperClaim1.setPaperDoi("doi.123");
        paperClaims.add(authorPaperClaim1);
        PM_AuthorPaperClaim authorPaperClaim2 = new PM_AuthorPaperClaim();
        authorPaperClaim2.setAuthorId(456);
        authorPaperClaim2.setPaperDoi("doi.456");
        paperClaims.add(authorPaperClaim2);
        ArrayList<Map<String, Object>> claims = new ArrayList<>();
        Map<String, Object> claim1 = new HashMap<>();
        claim1.put("id", 123);
        claim1.put("doi", "doi.123");
        claims.add(claim1);
        Map<String, Object> claim2 = new HashMap<>();
        claim2.put("id", 456);
        claim2.put("doi", "doi.456");
        claims.add(claim2);
        BDDMockito.given(adminRepository.findById(admin.getId())).willReturn(admin);
        BDDMockito.given(authorPaperClaimRepository.findAll()).willReturn(paperClaims);
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/papers/request/claim")
                                .header("Authorization", bearerToken)
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(200))
                .andExpect(jsonPath("claims").value(claims));
    }

    @Test
    public void approveClaim() throws Exception {
        // 非管理员
        String token = "validToken";
        String bearerToken = "Bearer " + token;
        PM_Admin admin = new PM_Admin();
        admin.setId(8080);
        PM_User user = new PM_User();
        user.setId(6060);
        String doi = "doi.abc";
        String doi64 = Base64.getEncoder().encodeToString(doi.getBytes());
        BDDMockito.given(jwtUtil.extractUsername(token)).willReturn(String.valueOf(admin.getId()));
        BDDMockito.given(adminRepository.findById(admin.getId())).willReturn(null);
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/papers/author")
                                .header("Authorization", bearerToken)
                                .param("id", String.valueOf(user.getId()))
                                .param("doi", doi64)
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(401));

        // 认领申请不存在
        BDDMockito.given(adminRepository.findById(admin.getId())).willReturn(admin);
        BDDMockito.given(authorPaperClaimRepository.findByAuthorIdAndPaperDoi(user.getId(), doi)).willReturn(null);
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/papers/author")
                                .header("Authorization", bearerToken)
                                .param("id", String.valueOf(user.getId()))
                                .param("doi", doi64)
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(404))
                .andExpect(jsonPath("message").value("Claim application not found"));

        // 论文不存在
        PM_AuthorPaperClaim authorPaperClaim = new PM_AuthorPaperClaim();
        authorPaperClaim.setAuthorId(user.getId());
        authorPaperClaim.setPaperDoi(doi);
        BDDMockito.given(authorPaperClaimRepository.findByAuthorIdAndPaperDoi(user.getId(), doi)).willReturn(authorPaperClaim);
        BDDMockito.given(paperRepository.findByDoi(doi)).willReturn(null);
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/papers/author")
                                .header("Authorization", bearerToken)
                                .param("id", String.valueOf(user.getId()))
                                .param("doi", doi64)
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(404))
                .andExpect(jsonPath("message").value("Paper not found"));

        // 成功
        PM_Paper paper = new PM_Paper();
        paper.setDoi(doi);
        user.setName("ming");
        PM_AuthorPaper authorPaper = new PM_AuthorPaper();
        String seq = "first";
        authorPaper.setAuthorId(user.getId());
        authorPaper.setPaperId(doi);
        authorPaper.setSeq(PM_AuthorPaper.Seq.valueOf(seq));
        BDDMockito.given(paperRepository.findByDoi(doi)).willReturn(paper);
        BDDMockito.given(userRepository.findById(user.getId())).willReturn(user);
        BDDMockito.given(paperService.getSeq(paper, user.getName())).willReturn(seq);
        BDDMockito.given(authorPaperRepository.save(authorPaper)).willReturn(authorPaper);
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/papers/author")
                                .header("Authorization", bearerToken)
                                .param("id", String.valueOf(user.getId()))
                                .param("doi", doi64)
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(200))
                .andExpect(jsonPath("message").value("Claim approved successfully"));

    }

    @Test
    public void deleteClaim() throws Exception {
        // 非管理员
        String token = "validToken";
        String bearerToken = "Bearer " + token;
        PM_Admin admin = new PM_Admin();
        admin.setId(8080);
        PM_User user = new PM_User();
        user.setId(6060);
        String doi = "doi.abc";
        String doi64 = Base64.getEncoder().encodeToString(doi.getBytes());
        BDDMockito.given(jwtUtil.extractUsername(token)).willReturn(String.valueOf(admin.getId()));
        BDDMockito.given(adminRepository.findById(admin.getId())).willReturn(null);
        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/papers/request/claim")
                                .header("Authorization", bearerToken)
                                .param("id", String.valueOf(user.getId()))
                                .param("doi", doi64)
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(401));

        // 认领申请不存在
        BDDMockito.given(adminRepository.findById(admin.getId())).willReturn(admin);
        BDDMockito.given(authorPaperClaimRepository.findByAuthorIdAndPaperDoi(user.getId(), doi)).willReturn(null);
        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/papers/request/claim")
                                .header("Authorization", bearerToken)
                                .param("id", String.valueOf(user.getId()))
                                .param("doi", doi64)
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(404))
                .andExpect(jsonPath("message").value("Claim application not found"));

        // 删除成功
        PM_AuthorPaperClaim authorPaperClaim = new PM_AuthorPaperClaim();
        authorPaperClaim.setAuthorId(user.getId());
        authorPaperClaim.setPaperDoi(doi);
        BDDMockito.given(authorPaperClaimRepository.findByAuthorIdAndPaperDoi(user.getId(), doi)).willReturn(authorPaperClaim);
        BDDMockito.doNothing().when(authorPaperClaimRepository).deleteByAuthorIdAndPaperDoi(user.getId(), doi);
        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/papers/request/claim")
                                .header("Authorization", bearerToken)
                                .param("id", String.valueOf(user.getId()))
                                .param("doi", doi64)
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(204))
                .andExpect(jsonPath("message").value("Claim application deleted successfully"));

    }

}