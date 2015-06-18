package com.neerpoints.service;

import com.neerpoints.context.ThreadContextService;
import com.neerpoints.model.PointsConfiguration;
import com.neerpoints.repository.PointsConfigurationRepository;
import com.neerpoints.service.model.ServiceResult;
import com.neerpoints.util.Translations;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PointsConfigurationService extends BaseService {
    private static final Logger logger = LogManager.getLogger(PointsConfigurationService.class.getName());
    private final PointsConfigurationRepository _pointsConfigurationRepository;

    @Autowired
    public PointsConfigurationService(PointsConfigurationRepository pointsConfigurationRepository, Translations translations,
        ThreadContextService threadContextService) {
        super(translations, threadContextService);
        _pointsConfigurationRepository = pointsConfigurationRepository;
    }

    public ServiceResult<PointsConfiguration> getByCompanyId(long companyId) {
        try {
            PointsConfiguration pointsConfiguration = _pointsConfigurationRepository.getByCompanyId(companyId);
            return new ServiceResult<>(true, "", pointsConfiguration);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return new ServiceResult<>(false, getTranslation(Translations.Message.COMMON_USER_ERROR), null);
        }
    }

    public ServiceResult<Boolean> update(PointsConfiguration pointsConfiguration) {
        try {
            int updatedRows = _pointsConfigurationRepository.update(pointsConfiguration);
            return new ServiceResult<>(true, getTranslation(Translations.Message.CONFIGURATION_UPDATED), updatedRows == 1);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return new ServiceResult<>(false, getTranslation(Translations.Message.COMMON_USER_ERROR), null);
        }
    }
}
