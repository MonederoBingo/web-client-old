package com.neerpoints.api.controller;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import com.neerpoints.model.Company;
import com.neerpoints.service.CompanyService;
import com.neerpoints.service.model.CompanyRegistration;
import com.neerpoints.service.model.ServiceResult;
import com.neerpoints.util.Translations;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.easymock.EasyMock;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class CompanyControllerTest {
    @Test
    public void testRegister() throws Exception {
        final ServiceResult expectedServiceResult = new ServiceResult(true, "1");
        final CompanyService companyService = createCompanyServiceForRegister(expectedServiceResult);
        final CompanyController companyController = new CompanyController(companyService);

        final CompanyRegistration companyRegistration = new CompanyRegistration();
        companyRegistration.setCompanyName("company name");
        companyRegistration.setUserName("user name");
        companyRegistration.setEmail("email@test.com");
        companyRegistration.setPassword("Pa$$w0rd");
        companyRegistration.setUrlImageLogo("images/logo.png");

        ResponseEntity<ServiceResult> responseEntity = companyController.register(companyRegistration);
        assertNotNull(responseEntity);
        ServiceResult actualServiceResults = responseEntity.getBody();
        assertNotNull(actualServiceResults);
        assertEquals(expectedServiceResult.isSuccess(), actualServiceResults.isSuccess());
        assertEquals(expectedServiceResult.getMessage(), actualServiceResults.getMessage());
        verify(companyService);
    }

    @Test
    public void testUpdateLogo() throws FileUploadException {
        CompanyService companyService = createCompanyService(new ServiceResult<Boolean>(true, Translations.Message.YOUR_LOGO_WAS_UPDATED.name()));
        final ServletFileUpload servletFileUpload = createMock(ServletFileUpload.class);
        expect(servletFileUpload.parseRequest((HttpServletRequest) anyObject())).andReturn(new ArrayList<FileItem>());
        replay(servletFileUpload);
        CompanyController companyController = new CompanyController(companyService) {
            @Override
            ServletFileUpload getServletFileUpload() {
                return servletFileUpload;
            }
        };

        ResponseEntity<ServiceResult> responseEntity = companyController.updateLogo(1, createMock(HttpServletRequest.class));
        assertNotNull(responseEntity);
        ServiceResult serviceResult = responseEntity.getBody();
        assertNotNull(serviceResult);
        assertTrue(serviceResult.isSuccess());
        assertEquals(Translations.Message.YOUR_LOGO_WAS_UPDATED.name(), serviceResult.getMessage());
        verify(companyService);
    }

    @Test
    public void testGet() throws Exception {
        Company company = new Company();
        company.setName("name");
        company.setUrlImageLogo("logo.png");
        ServiceResult<Company> serviceResult = new ServiceResult<>(true, "", company);
        CompanyService companyService = createCompanyServiceForGet(serviceResult);
        CompanyController companyController = new CompanyController(companyService);
        ResponseEntity<ServiceResult<Company>> responseEntity = companyController.get(1);
        assertNotNull(responseEntity);
        ServiceResult actualServiceResult = responseEntity.getBody();
        assertNotNull(actualServiceResult);
        assertTrue(actualServiceResult.isSuccess());
        assertNotNull(actualServiceResult.getObject());
        Company actualCompany = serviceResult.getObject();
        assertEquals("name", actualCompany.getName());
        assertEquals("logo.png", actualCompany.getUrlImageLogo());
        verify(companyService);
    }

    private CompanyService createCompanyService(ServiceResult<Boolean> serviceResult) {
        CompanyService companyService = createMock(CompanyService.class);
        expect(companyService.updateLogo((List<FileItem>) anyObject(), anyLong())).andReturn(serviceResult);
        replay(companyService);
        return companyService;
    }

    private CompanyService createCompanyServiceForRegister(ServiceResult serviceResult) throws Exception {
        final CompanyService companyService = EasyMock.createMock(CompanyService.class);
        expect(companyService.register((CompanyRegistration) anyObject())).andReturn(serviceResult).times(1);
        replay(companyService);
        return companyService;
    }

    private CompanyService createCompanyServiceForGet(ServiceResult<Company> company) throws Exception {
        final CompanyService companyService = EasyMock.createMock(CompanyService.class);
        expect(companyService.getByCompanyId(anyLong())).andReturn(company).times(1);
        replay(companyService);
        return companyService;
    }
}