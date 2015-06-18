package com.neerpoints.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.neerpoints.context.ThreadContextService;
import com.neerpoints.db.QueryAgent;
import com.neerpoints.model.Client;
import com.neerpoints.model.ClientPoints;
import com.neerpoints.model.CompanyClientMapping;
import com.neerpoints.repository.ClientRepository;
import com.neerpoints.repository.CompanyClientMappingRepository;
import com.neerpoints.service.model.ClientRegistration;
import com.neerpoints.service.model.ServiceResult;
import com.neerpoints.util.Translations;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class ClientServiceTest {
    @Test
    public void testRegister() throws Exception {
        Client client = new Client();
        client.setClientId(1);
        final ClientRepository clientRepository = createClientRepository(client);
        final QueryAgent queryAgent = createQueryAgent();
        final ThreadContextService threadContextService = createThreadContextService(queryAgent);
        final CompanyClientMappingRepository companyClientMappingRepository = createCompanyClientMappingRepositoryForInsert();
        final ClientService clientService = new ClientService(clientRepository, companyClientMappingRepository, threadContextService, null) {
            @Override
            String getTranslation(Translations.Message message) {
                return message.name();
            }
        };
        ClientRegistration clientRegistration = new ClientRegistration();
        clientRegistration.setCompanyId(1);
        clientRegistration.setPhone("6141112233");
        ServiceResult serviceResult = clientService.register(clientRegistration);
        assertNotNull(serviceResult);
        assertTrue(serviceResult.isSuccess());
        assertEquals(1l, serviceResult.getObject());
        verify(clientRepository, companyClientMappingRepository);
    }

    @Test
    public void testRegisterWithInvalidPhone() {
        final ClientService clientService = new ClientService(null, null, null, null) {
            @Override
            String getTranslation(Translations.Message message) {
                return message.name();
            }
        };
        ClientRegistration clientRegistration = new ClientRegistration();
        clientRegistration.setPhone("123");
        ServiceResult<Long> serviceResult = clientService.register(clientRegistration);
        assertNotNull(serviceResult);
        assertEquals(Translations.Message.PHONE_MUST_HAVE_10_DIGITS.name(), serviceResult.getMessage());
    }


    @Test
    public void testRegisterWhenThereIsAnExistentMapping() throws Exception {
        final ClientRepository clientRepository = createClientRepositoryWhenThereIsAnExistentMapping();
        final CompanyClientMappingRepository companyClientMappingRepository = createCompanyClientMappingRepositoryWhenThereIsAnExistentMapping();
        final ClientService clientService = new ClientService(clientRepository, companyClientMappingRepository, null, null) {
            @Override
            String getTranslation(Translations.Message message) {
                return message.name();
            }
        };

        final ClientRegistration clientRegistration = new ClientRegistration();
        clientRegistration.setPhone("1234567890");
        ServiceResult serviceResult = clientService.register(clientRegistration);
        assertNotNull(serviceResult);
        assertFalse(serviceResult.isSuccess());
        assertEquals(Translations.Message.THE_CLIENT_ALREADY_EXISTS.name(), serviceResult.getMessage());
        assertNull(serviceResult.getObject());
        verify(clientRepository, companyClientMappingRepository);
    }

    @Test
    public void testGetByCompanyId() throws Exception {
        final List<ClientPoints> expectedClients = new ArrayList<>();
        expectedClients.add(createClient(100, "6391112233"));
        expectedClients.add(createClient(200, "6141112233"));
        final ClientRepository clientRepository = createClientRepositoryForGet(expectedClients);
        final ClientService clientService = new ClientService(clientRepository, null, null, null);

        ServiceResult<List<ClientPoints>> serviceResult = clientService.getByCompanyId(1);
        assertNotNull(serviceResult);
        assertTrue(serviceResult.isSuccess());
        assertEquals("", serviceResult.getMessage());
        assertNotNull(serviceResult.getObject());

        List<ClientPoints> actualClients = serviceResult.getObject();
        assertEquals(2, actualClients.size());
        assertEquals(100, actualClients.get(0).getPoints(), 0.00);
        assertEquals("6391112233", actualClients.get(0).getPhone());
        assertEquals(200, actualClients.get(1).getPoints(), 0.00);
        assertEquals("6141112233", actualClients.get(1).getPhone());
        verify(clientRepository);
    }

    @Test
    public void testGetByCompanyIdPhone() throws Exception {
        final ClientPoints clientPoints = new ClientPoints();
        clientPoints.setPoints(1200);
        clientPoints.setPhone("1234567890");
        final ClientRepository clientRepository = createClientRepositoryForGetByCompanyIDPhone(clientPoints);
        final ClientService clientService = new ClientService(clientRepository, null, null, null);

        ServiceResult<ClientPoints> serviceResult = clientService.getByCompanyIdPhone(1, "1234567890");
        assertNotNull(serviceResult);
        assertTrue(serviceResult.isSuccess());
        assertEquals("", serviceResult.getMessage());
        assertNotNull(serviceResult.getObject());

        final ClientPoints actualClientPoints = serviceResult.getObject();
        assertEquals(1200, actualClientPoints.getPoints(), 0.00);
        assertEquals("1234567890", actualClientPoints.getPhone());
        verify(clientRepository);
    }

    private ThreadContextService createThreadContextService(QueryAgent queryAgent) throws SQLException {
        ThreadContextService threadContextService = createMock(ThreadContextService.class);

        expect(threadContextService.getQueryAgent()).andReturn(queryAgent).times(2);
        replay(threadContextService);
        return threadContextService;
    }

    private QueryAgent createQueryAgent() throws Exception {
        QueryAgent queryAgent = createMock(QueryAgent.class);
        queryAgent.beginTransaction();
        expectLastCall().times(1);
        queryAgent.commitTransaction();
        expectLastCall().times(1);
        replay(queryAgent);
        return queryAgent;
    }

    private ClientPoints createClient(float points, String phone) {
        ClientPoints clientPoints = new ClientPoints();
        clientPoints.setPoints(points);
        clientPoints.setPhone(phone);
        return clientPoints;
    }

    private CompanyClientMappingRepository createCompanyClientMappingRepositoryForInsert() throws Exception {
        CompanyClientMappingRepository companyClientMappingRepository = createMock(CompanyClientMappingRepository.class);
        expect(companyClientMappingRepository.insert((CompanyClientMapping) anyObject())).andReturn(1l);
        expect(companyClientMappingRepository.getByCompanyIdClientId(anyLong(), anyLong())).andReturn(null);
        replay(companyClientMappingRepository);
        return companyClientMappingRepository;
    }

    private CompanyClientMappingRepository createCompanyClientMappingRepositoryWhenThereIsAnExistentMapping() throws Exception {
        CompanyClientMappingRepository companyClientMappingRepository = createMock(CompanyClientMappingRepository.class);
        expect(companyClientMappingRepository.getByCompanyIdClientId(anyLong(), anyLong())).andReturn(new CompanyClientMapping());
        replay(companyClientMappingRepository);
        return companyClientMappingRepository;
    }

    private ClientRepository createClientRepository(Client client) throws Exception {
        ClientRepository clientRepository = createMock(ClientRepository.class);
        expect(clientRepository.insertIfDoesNotExist(anyString())).andReturn(client).anyTimes();
        expect(clientRepository.getByPhone(anyString())).andReturn(client).anyTimes();
        replay(clientRepository);
        return clientRepository;
    }

    private ClientRepository createClientRepositoryWhenThereIsAnExistentMapping() throws Exception {
        ClientRepository clientRepository = createMock(ClientRepository.class);
        expect(clientRepository.getByPhone(anyString())).andReturn(new Client()).anyTimes();
        replay(clientRepository);
        return clientRepository;
    }

    private ClientRepository createClientRepositoryForGet(List<ClientPoints> clientPointsList) throws Exception {
        ClientRepository clientRepository = createMock(ClientRepository.class);
        expect(clientRepository.getByCompanyId(anyLong())).andReturn(clientPointsList).anyTimes();
        replay(clientRepository);
        return clientRepository;
    }

    private ClientRepository createClientRepositoryForGetByCompanyIDPhone(ClientPoints clientPoints) throws Exception {
        ClientRepository clientRepository = createMock(ClientRepository.class);
        expect(clientRepository.getByCompanyIdPhone(anyLong(), anyString())).andReturn(clientPoints).anyTimes();
        replay(clientRepository);
        return clientRepository;
    }
}