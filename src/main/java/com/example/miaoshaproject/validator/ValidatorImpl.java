package com.example.miaoshaproject.validator;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

@Component
public class ValidatorImpl implements InitializingBean {


    private Validator validator;

    /*
        Springbean 初始化对象完成之后，会调用这个方法
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        // 这个bean创建好，就带有这个属性了
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    public ValidationResult validate(Object obj) {
        ValidationResult result = new ValidationResult();
        Set<ConstraintViolation<Object>> set = validator.validate(obj);
        if (set.size() > 0) {
            result.setHasErrors(true);
            set.forEach(item -> {
                String errorMessage = item.getMessage();
                String propertyName = item.getPropertyPath().toString();
                result.getErrMsgMap().put(propertyName, errorMessage);
            });
        }
        return result;
    }
}
