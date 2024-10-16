package cn.zwz.paper.entity;

import cn.zwz.basics.baseClass.ZwzBaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "paper")
@TableName("paper")
@ApiModel(value = "论文")
public class Paper extends ZwzBaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "论文序列号")
    private String paper_id;

    @ApiModelProperty(value = "论文名称")
    private String title;

    @ApiModelProperty(value = "论文摘要")
    private String Abstract; //abstract 是java关键词

    @ApiModelProperty(value = "关键词")
    private String keywords;

    @ApiModelProperty(value = "发表时间")
    private Date publication_data;

    @ApiModelProperty(value = "论文页数")
    private int page_count;

    @ApiModelProperty(value = "会议名称")
    private String meeting;

    @ApiModelProperty(value = "期刊名称")
    private String periodical;

    @ApiModelProperty(value = "出版商")
    private String publisher;

    @ApiModelProperty(value = "状态")
    private String status;

    @ApiModelProperty(value = "驳回意见")
    private String recommend;

    //@ApiModelProperty(value = "是否被选择")
    //private boolean checkFlag;

    //@ApiModelProperty(value = "选题人")
    //private String checkId;

    //@ApiModelProperty(value = "选题人")
    //private String checkName;

    //@ApiModelProperty(value = "是否被审核")
    //private boolean auditFlag;
}