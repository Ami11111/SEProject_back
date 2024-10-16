package cn.zwz.paper.serviceimpl;

import cn.zwz.paper.entity.Paper;
import cn.zwz.paper.mapper.PaperMapper;
import cn.zwz.paper.service.IPaperService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 毕业设计课题 服务层接口实现
 * @author 郑为中
 */
@Slf4j
@Service
@Transactional
public class IPaperServiceImpl extends ServiceImpl<PaperMapper, Paper> implements IPaperService {

    @Autowired
    private PaperMapper topicMapper;
}