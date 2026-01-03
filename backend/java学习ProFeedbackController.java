import com.ktg.mes.pro.service.IProFeedbackService;
import com.ktg.common.utils.poi.ExcelUtil;
import com.ktg.common.core.page.TableDataInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ktg.system.service.ISysConfigService;

/**
 * ����������¼Controller
@@ -51,6 +54,8 @@ import com.ktg.common.core.page.TableDataInfo;
@RequestMapping("/mes/pro/feedback")
public class ProFeedbackController extends BaseController
{
    private static final Logger logger = LoggerFactory.getLogger(ProFeedbackController.class);
    学习
调用LoggerFactory类的getLogger静态工厂方法，传入ProFeedbackController.class作为参数，创建并返回一个Logger接口的实现对象，然后将这个对象引用赋值给logger变量"
即为ProFeedbackController类创建一个专属的日志记录器，并赋值给logger变量。
ProFeedbackController.class是参数，告诉logger是为哪个类服务的


    @Autowired
    private IProFeedbackService proFeedbackService;

@@ -90,6 +95,9 @@ public class ProFeedbackController extends BaseController
    @Autowired
    private AutoCodeUtil autoCodeUtil;

    @Autowired
    private ISysConfigService configService;

    /**
     * ��ѯ����������¼�б�
     */
@@ -130,6 +138,7 @@ public class ProFeedbackController extends BaseController
    @PreAuthorize("@ss.hasPermi('mes:pro:feedback:add')")
    @Log(title = "����������¼", businessType = BusinessType.INSERT)
    @PostMapping
    @Transactional 学习：该注解确实保证方法内的所有数据库操作要么全部成功提交，要么全部回滚，这是通过Spring的AOP代理和数据库事务机制实现的。
    public AjaxResult add(@RequestBody ProFeedback proFeedback)
    {
        MdWorkstation workstation = mdWorkstationService.selectMdWorkstationByWorkstationId(proFeedback.getWorkstationId());
@@ -161,6 +170,68 @@ public class ProFeedbackController extends BaseController
            return AjaxResult.error("��ǰ���������״̬Ϊ����ɣ����ܼ���������");
        }

        // ========== �������������þ���״̬ ==========
        String checkMode = configService.selectConfigByKey("mes.feedback.overproduce.checkmode");
        if(StringUtils.isEmpty(checkMode)){
            checkMode = "2";  // Ĭ��ʹ�������߼�
        }
        
        // ����Ƿ���Ҫ�Զ�ִ�����
        boolean autoExecuteEnabled = false;
        
        // �ж��Ƿ��йؼ��������Ҫ����
        boolean isKeyProcess = proRouteProcessService.checkKeyProcess(proFeedback);
        boolean needCheck = UserConstants.YES.equals(proFeedback.getIsCheck());
        
        if("0".equals(checkMode)){
            // ģʽ0�������Զ����(��ȫ�������)
            if(!isKeyProcess && !needCheck){
                proFeedback.setStatus("APPROVING");
                autoExecuteEnabled = true;
            } else {
                // �йؼ��������Ҫ���飬����ԭ����
                autoExecuteEnabled = false;
            }
            
        } else if("1".equals(checkMode)){
            // ģʽ1���Զ���ˣ��������ƻ������򱣳�ԭ��
            if(!isKeyProcess && !needCheck){
                BigDecimal quantityProduced = task.getQuantityProduced() != null ? task.getQuantityProduced() : BigDecimal.ZERO;
                BigDecimal quantityFeedback = proFeedback.getQuantityFeedback() != null ? proFeedback.getQuantityFeedback() : BigDecimal.ZERO;
                BigDecimal planQuantity = null;
                
                if(workorder != null && workorder.getQuantity() != null){
                    planQuantity = workorder.getQuantity();
                } else if(task.getQuantity() != null){
                    planQuantity = task.getQuantity();
                }
                
                if(planQuantity != null){
                    BigDecimal totalAfterFeedback = quantityProduced.add(quantityFeedback);
                    if(totalAfterFeedback.compareTo(planQuantity) > 0){
                        // ����������ԭ��״̬ΪPREPARE����Ҫ�ֶ��ύ��ˣ�
                        proFeedback.setStatus("PREPARE");
                        autoExecuteEnabled = false;
                    } else {
                        // δ�������Զ����
                        proFeedback.setStatus("APPROVING");
                        autoExecuteEnabled = true;
                    }
                } else {
                    // �޼ƻ��������Զ����
                    proFeedback.setStatus("APPROVING");
                    autoExecuteEnabled = true;
                }
            } else {
                // �йؼ��������Ҫ���飬����ԭ����
                autoExecuteEnabled = false;
            }
        } else {
            // ģʽ2��������Ŀ�����߼�
            // ������status������ԭ��Ĭ��ֵ
        }
        // ========== �������� ==========

        //�������
        if(UserConstants.YES.equals(proFeedback.getIsCheck())){
            if(!StringUtils.isNotNull(proFeedback.getQuantityFeedback()) || proFeedback.getQuantityFeedback().compareTo(BigDecimal.ZERO) <= 0 ){
@@ -174,7 +245,23 @@ public class ProFeedbackController extends BaseController
        String feedbackCode = autoCodeUtil.genSerialCode(UserConstants.FEEDBACK_CODE,"");
        proFeedback.setFeedbackCode(feedbackCode);
        proFeedback.setCreateBy(getUsername());
        return toAjax(proFeedbackService.insertProFeedback(proFeedback));
        
        // ���汨����¼
        int result = proFeedbackService.insertProFeedback(proFeedback);
        
        // �����Ҫ�Զ�ִ�����
        if(autoExecuteEnabled && result > 0){
            try {
                // ֱ�ӵ������е�execute����
                return execute(proFeedback.getRecordId());
            } catch (Exception e) {
                // �Զ�ִ��ʧ�ܣ����ر���ɹ�����Ҫ�ֶ����
                logger.error("�Զ����ʧ��", e);
                return AjaxResult.success("��������ɹ������Զ����ʧ�ܣ����ֶ�ִ�����");
            }
        }
        
        return toAjax(result);
    }

    /**
@@ -316,7 +403,6 @@ public class ProFeedbackController extends BaseController
        return AjaxResult.success();
    }


    /**
     * ִ���������Ŀ�涯��
     * @param record
@@ -328,8 +414,4 @@ public class ProFeedbackController extends BaseController
        record.setStatus(UserConstants.ORDER_STATUS_FINISHED);
        wmItemConsumeService.updateWmItemConsume(record);
    }




}
}
\ No newline at end of file