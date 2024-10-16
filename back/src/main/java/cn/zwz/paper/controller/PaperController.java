package cn.zwz.paper.controller;

import cn.zwz.basics.baseVo.PageVo;
import cn.zwz.basics.baseVo.Result;
import cn.zwz.basics.utils.PageUtil;
import cn.zwz.basics.utils.ResultUtil;
import cn.zwz.basics.utils.SecurityUtil;
import cn.zwz.data.entity.User;
import cn.zwz.data.utils.ZwzNullUtils;
import cn.zwz.paper.entity.Paper;
import cn.zwz.paper.service.IPaperService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@Api(tags = "论文管理接口")
@RequestMapping("/zwz/paper")
@Transactional
public class PaperController {

    @Autowired
    private IPaperService iPaperService;

    @Autowired
    private SecurityUtil securityUtil;

    @RequestMapping(value = "/getOne", method = RequestMethod.GET)
    @ApiOperation(value = "查询单条论文")
    public Result<Paper> get(@RequestParam String id){
        return new ResultUtil<Paper>().setData(iPaperService.getById(id));
    }

    @RequestMapping(value = "/count", method = RequestMethod.GET)
    @ApiOperation(value = "查询全部论文个数")
    public Result<Long> getCount(){
        return new ResultUtil<Long>().setData(iPaperService.count());
    }

    @RequestMapping(value = "/getAll", method = RequestMethod.GET)
    @ApiOperation(value = "查询全部论文")
    public Result<List<Paper>> getAll(){
        return new ResultUtil<List<Paper>>().setData(iPaperService.list());
    }

    //需要用两张表，未解决
    /*
    @RequestMapping(value = "/getByPage", method = RequestMethod.GET)
    @ApiOperation(value = "查询我发布的论文")
    public Result<IPage<Paper>> getByPage(@ModelAttribute Paper topic , @ModelAttribute PageVo page){
        QueryWrapper<Paper> qw = new QueryWrapper<>();
        if(!ZwzNullUtils.isNull(topic.getTitle())) {
            qw.like("title",topic.getTitle());
        }
        qw.eq("tea_id",securityUtil.getCurrUser().getId());
        IPage<Paper> data = iPaperService.page(PageUtil.initMpPage(page),qw);
        return new ResultUtil<IPage<Paper>>().setData(data);
    }

     */

    @RequestMapping(value = "/getByTitle", method = RequestMethod.GET)
    @ApiOperation(value = "标题查询论文")
    public Result<IPage<Paper>> getByTitle(@ModelAttribute Paper topic , @ModelAttribute PageVo page){
        QueryWrapper<Paper> qw = new QueryWrapper<>();
        if(!ZwzNullUtils.isNull(topic.getTitle())) {
            qw.like("title",topic.getTitle());
        }
        IPage<Paper> data = iPaperService.page(PageUtil.initMpPage(page),qw);
        return new ResultUtil<IPage<Paper>>().setData(data);
    }

/*
    @RequestMapping(value = "/getByMyPage", method = RequestMethod.GET)
    @ApiOperation(value = "查询我选的论文")
    public Result<IPage<Paper>> getByMyPage(@ModelAttribute Paper topic , @ModelAttribute PageVo page){
        QueryWrapper<Paper> qw = new QueryWrapper<>();
        if(!ZwzNullUtils.isNull(topic.getTitle())) {
            qw.like("title",topic.getTitle());
        }
        if(!ZwzNullUtils.isNull(topic.getType())) {
            qw.eq("type",topic.getType());
        }
        if(!ZwzNullUtils.isNull(topic.getLevel())) {
            qw.eq("level",topic.getLevel());
        }
        qw.eq("check_flag",true);
        qw.eq("check_id",securityUtil.getCurrUser().getId());
        IPage<Paper> data = iPaperService.page(PageUtil.initMpPage(page),qw);
        return new ResultUtil<IPage<Paper>>().setData(data);
    }

    @RequestMapping(value = "/insertOrUpdate", method = RequestMethod.POST)
    @ApiOperation(value = "增改论文")
    public Result<Paper> saveOrUpdate(Paper topic){
        if(iPaperService.saveOrUpdate(topic)){
            return new ResultUtil<Paper>().setData(topic);
        }
        return ResultUtil.error();
    }

    @RequestMapping(value = "/insert", method = RequestMethod.POST)
    @ApiOperation(value = "新增论文")
    public Result<Paper> insert(Paper topic){
        User currUser = securityUtil.getCurrUser();
        topic.setTeaId(currUser.getId());
        topic.setTeaName(currUser.getNickname());
        iPaperService.saveOrUpdate(topic);
        return new ResultUtil<Paper>().setData(topic);
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ApiOperation(value = "编辑论文")
    public Result<Paper> update(Paper topic){
        iPaperService.saveOrUpdate(topic);
        return new ResultUtil<Paper>().setData(topic);
    }

    @RequestMapping(value = "/delByIds", method = RequestMethod.POST)
    @ApiOperation(value = "删除论文")
    public Result<Object> delByIds(@RequestParam String[] ids){
        for(String id : ids){
            iPaperService.removeById(id);
        }
        return ResultUtil.success();
    }

    @RequestMapping(value = "/checkTopic", method = RequestMethod.POST)
    @ApiOperation(value = "选择论文")
    public Result<Object> checkTopic(@RequestParam String id){
        Paper topic = iPaperService.getById(id);
        if(topic == null) {
            return ResultUtil.error("论文不存在");
        }
        User currUser = securityUtil.getCurrUser();
        topic.setCheckFlag(true);
        topic.setCheckId(currUser.getId());
        topic.setCheckName(currUser.getNickname());
        iPaperService.saveOrUpdate(topic);
        return ResultUtil.success();
    }

    @RequestMapping(value = "/checkNotTopic", method = RequestMethod.POST)
    @ApiOperation(value = "取消选择论文")
    public Result<Object> checkNotTopic(@RequestParam String id){
        Paper topic = iPaperService.getById(id);
        if(topic == null) {
            return ResultUtil.error("论文不存在");
        }
        topic.setCheckFlag(false);
        topic.setCheckId("");
        topic.setCheckName("");
        topic.setAuditFlag(false);
        iPaperService.saveOrUpdate(topic);
        return ResultUtil.success();
    }

    @RequestMapping(value = "/auditTopic", method = RequestMethod.POST)
    @ApiOperation(value = "审核课题")
    public Result<Object> auditTopic(@RequestParam String id){
        Paper topic = iPaperService.getById(id);
        if(topic == null) {
            return ResultUtil.error("论文不存在");
        }
        topic.setAuditFlag(true);
        iPaperService.saveOrUpdate(topic);
        return ResultUtil.success();
    }

    @RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
    @ApiOperation(value = "上传论文书")
    public Result<Object> uploadFile(@RequestParam String id,@RequestParam String file){
        Paper topic = iPaperService.getById(id);
        if(topic == null) {
            return ResultUtil.error("论文不存在");
        }
        topic.setTaskFile(file);
        iPaperService.saveOrUpdate(topic);
        return ResultUtil.success();
    }

 */
}
