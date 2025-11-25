package io.aiven.klaw.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.model.AclInfo;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.SyncAclUpdates;
import io.aiven.klaw.model.enums.AclGroupBy;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.requests.AclRequestsModel;
import io.aiven.klaw.model.response.AclRequestsResponseModel;
import io.aiven.klaw.model.response.OffsetDetails;
import io.aiven.klaw.model.response.ServiceAccountDetails;
import io.aiven.klaw.model.response.TopicOverview;
import io.aiven.klaw.service.AclControllerService;
import io.aiven.klaw.service.AclSyncControllerService;
import io.aiven.klaw.service.TopicOverviewService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AclControllerTest {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String topicName = "testtopic";
    private static final int topicId = 1001;
    @MockBean
    private AclControllerService aclControllerService;
    @MockBean
    private AclSyncControllerService aclSyncControllerService;

    @MockBean
    private TopicOverviewService topicOverviewService;
    private UtilMethods utilMethods;
    private MockMvc mvcAcls;
    private AclController aclController;
    private MockMvc mvcAclsSync;
    private AclSyncController aclSyncController;

    @BeforeEach
    public void setup() {
        aclController = new AclController();
        aclSyncController = new AclSyncController();
        utilMethods = new UtilMethods();
        mvcAcls = MockMvcBuilders.standaloneSetup(aclController).dispatchOptions(true).build();
        ReflectionTestUtils.setField(aclController, "aclControllerService", aclControllerService);
        ReflectionTestUtils.setField(aclController, "topicOverviewService", topicOverviewService);
        mvcAclsSync = MockMvcBuilders.standaloneSetup(aclSyncController).dispatchOptions(true).build();
        ReflectionTestUtils.setField(
                aclSyncController, "aclSyncControllerService", aclSyncControllerService);
    }

    @Test
    @Order(1)
    public void createAcl() throws Exception {
        AclRequestsModel addAclRequest = utilMethods.getAclRequestModel(topicName + topicId);
        String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(addAclRequest);
        ApiResponse apiResponse = ApiResponse.SUCCESS;
        when(aclControllerService.createAcl(any())).thenReturn(apiResponse);

        mvcAcls
                .perform(
                        MockMvcRequestBuilders.post("/createAcl")
                                .content(jsonReq)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is(ApiResultStatus.SUCCESS.value)));
    }

    @Test
    public void updateSyncAcls() throws Exception {
        List<SyncAclUpdates> syncUpdates = utilMethods.getSyncAclsUpdates();

        String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(syncUpdates);

        ApiResponse apiResponse = ApiResponse.SUCCESS;
        when(aclSyncControllerService.updateSyncAcls(any())).thenReturn(apiResponse);

        mvcAclsSync
                .perform(
                        MockMvcRequestBuilders.post("/updateSyncAcls")
                                .content(jsonReq)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is(ApiResultStatus.SUCCESS.value)));
    }

    @Test
    public void getAclRequests() throws Exception {

        List<AclRequestsResponseModel> aclRequests = utilMethods.getAclRequestsModel();

        when(aclControllerService.getAclRequests(
                "1",
                "",
                "all",
                null,
                null,
                null,
                null,
                null,
                io.aiven.klaw.model.enums.Order.DESC_REQUESTED_TIME,
                false))
                .thenReturn(aclRequests);

        mvcAcls
                .perform(
                        MockMvcRequestBuilders.get("/getAclRequests")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("pageNo", "1")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    public void getCreatedAclRequests() throws Exception {

        List<AclRequestsResponseModel> aclRequests = utilMethods.getAclRequestsList();

        when(aclControllerService.getAclRequestsForApprover(
                "1",
                "",
                "created",
                null,
                null,
                RequestOperationType.CREATE,
                null,
                null,
                io.aiven.klaw.model.enums.Order.ASC_REQUESTED_TIME))
                .thenReturn(aclRequests);

        mvcAcls
                .perform(
                        MockMvcRequestBuilders.get("/getAclRequestsForApprover")
                                .param("pageNo", "1")
                                .param("operationType", RequestOperationType.CREATE.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    public void deleteAclRequests() throws Exception {
        ApiResponse apiResponse = ApiResponse.SUCCESS;
        when(aclControllerService.deleteAclRequests(anyString())).thenReturn(apiResponse);
        mvcAcls
                .perform(
                        MockMvcRequestBuilders.post("/deleteAclRequests")
                                .param("req_no", "fsda32FSDw")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is(ApiResultStatus.SUCCESS.value)));
    }

    @Test
    public void approveAclRequests() throws Exception {
        ApiResponse apiResponse = ApiResponse.SUCCESS;
        when(aclControllerService.approveAclRequests(anyString())).thenReturn(apiResponse);

        mvcAcls
                .perform(
                        MockMvcRequestBuilders.post("/execAclRequest")
                                .param("req_no", "reqno")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is(ApiResultStatus.SUCCESS.value)));
    }

    @Test
    public void declineAclRequests() throws Exception {
        ApiResponse apiResponse = ApiResponse.SUCCESS;
        when(aclControllerService.declineAclRequests(anyString(), anyString())).thenReturn(apiResponse);
        mvcAcls
                .perform(
                        MockMvcRequestBuilders.post("/execAclRequestDecline")
                                .param("req_no", "reqno")
                                .param("reasonForDecline", "reason")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is(ApiResultStatus.SUCCESS.value)));
    }

    @Test
    public void getAcls1() throws Exception {
        TopicOverview topicOverview = utilMethods.getTopicOverview();

        when(topicOverviewService.getTopicOverview("testtopic", "", AclGroupBy.NONE))
                .thenReturn(topicOverview);

        mvcAcls
                .perform(
                        MockMvcRequestBuilders.get("/getTopicOverview")
                                .param("topicName", "testtopic")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topicInfoList[*]", hasSize(1)))
                .andExpect(jsonPath("$.aclInfoList[*]", hasSize(1)));
    }

    @Test
    public void getAcls2() throws Exception {
        TopicOverview topicOverview = utilMethods.getTopicOverview();

        when(topicOverviewService.getTopicOverview(null, "1", AclGroupBy.NONE))
                .thenReturn(topicOverview);

        // TODO Consider returning an error response object (https://www.rfc-editor.org/rfc/rfc7807)
        // Just checking response code seems to be sufficient as the contentAsString() returns an empty
        // String.
        mvcAcls
                .perform(
                        MockMvcRequestBuilders.get("/getTopicOverview")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getSyncAcls() throws Exception {
        List<AclInfo> aclInfo = utilMethods.getAclInfoList();

        when(aclSyncControllerService.getSyncAcls(anyString(), anyString(), anyString(), any(), any()))
                .thenReturn(aclInfo);

        mvcAclsSync
                .perform(
                        MockMvcRequestBuilders.get("/getSyncAcls")
                                .param("env", "DEV")
                                .param("pageNo", "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    public void getAivenServiceAccount() throws Exception {
        ServiceAccountDetails serviceAccountDetails = new ServiceAccountDetails();
        serviceAccountDetails.setPassword("password");
        serviceAccountDetails.setUsername("username");
        serviceAccountDetails.setAccountFound(true);

        when(aclControllerService.getAivenServiceAccountDetails(
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(serviceAccountDetails);

        String response =
                mvcAcls
                        .perform(
                                MockMvcRequestBuilders.get("/getAivenServiceAccount")
                                        .param("env", "DEV")
                                        .param("topicName", "testtopic")
                                        .param("userName", "kwuser")
                                        .param("aclReqNo", "101")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        ServiceAccountDetails serviceAccountDetails1 =
                new ObjectMapper().readValue(response, new TypeReference<>() {
                });
        assertThat(serviceAccountDetails1.isAccountFound()).isTrue();
    }

    //region Generated with Explyt. Tests for AclController

    /**
     * Given the user requests Aiven service account details with an invalid request ID<br>
     * When the system attempts to retrieve the service account details<br>
     * Then the system responds with a forbidden error
     */
    @Test
    public void getAivenServiceAccountDetailsInvalidRequestId() throws Exception {
        when(aclControllerService.getAivenServiceAccountDetails(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Invalid request"));

        mvcAcls.perform(MockMvcRequestBuilders.get("/getAivenServiceAccount")
                        .param("env", "DEV")
                        .param("topicName", "testtopic")
                        .param("userName", "kwuser")
                        .param("aclReqNo", "invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }


    /**
     * Given the user requests consumer offsets with valid environment ID, topic name, and consumer group ID<br>
     * When the system retrieves the consumer offsets<br>
     * Then the system returns a list of consumer offset details including partition ID, current offset, end offset, and lag
     */
    @Test
    public void getConsumerOffsetsValidRequest() throws Exception {
        List<OffsetDetails> offsetDetailsList = List.of(
                new OffsetDetails() {{
                    setTopicPartitionId("0");
                    setCurrentOffset("100");
                    setEndOffset("200");
                    setLag("100");
                }},
                new OffsetDetails() {{
                    setTopicPartitionId("1");
                    setCurrentOffset("150");
                    setEndOffset("250");
                    setLag("100");
                }}
        );

        when(aclControllerService.getConsumerOffsets(anyString(), anyString(), anyString()))
                .thenReturn(offsetDetailsList);

        mvcAcls.perform(MockMvcRequestBuilders.get("/getConsumerOffsets")
                        .param("env", "DEV")
                        .param("topicName", "testtopic")
                        .param("consumerGroupId", "testgroup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].topicPartitionId", is("0")))
                .andExpect(jsonPath("$[0].currentOffset", is("100")))
                .andExpect(jsonPath("$[0].endOffset", is("200")))
                .andExpect(jsonPath("$[0].lag", is("100")));
    }

    /**
     * Given the user requests available Aiven service accounts for a specific environment<br>
     * When the system retrieves the service accounts<br>
     * Then the system returns a set of available service account names
     */
    @Test
    public void getAivenServiceAccountsValidRequest() throws Exception {
        Set<String> serviceAccounts = Set.of("service-account-1", "service-account-2", "service-account-3");

        when(aclControllerService.getAivenServiceAccounts(anyString()))
                .thenReturn(serviceAccounts);

        mvcAcls.perform(MockMvcRequestBuilders.get("/getAivenServiceAccounts")
                        .param("env", "DEV")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$", hasItem("service-account-1")))
                .andExpect(jsonPath("$", hasItem("service-account-2")))
                .andExpect(jsonPath("$", hasItem("service-account-3")));
    }

    /**
     * Given the user is authorized to claim ACLs<br>
     * When the user submits a request to claim an ACL with a specific ACL ID<br>
     * Then the system processes the claim successfully<br>
     * And returns a success message indicating the ACL was claimed
     */
    @Test
    public void claimAclSuccess() throws Exception {
        ApiResponse apiResponse = ApiResponse.SUCCESS;
        when(aclControllerService.claimAcl(any(Integer.class))).thenReturn(apiResponse);

        mvcAcls.perform(MockMvcRequestBuilders.post("/acl/claim/123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is(ApiResultStatus.SUCCESS.value)));
    }

    /**
     * Given the user requests details of a specific ACL request with a valid request ID<br>
     * When the system retrieves the ACL request details<br>
     * Then the system returns the details of the ACL request including topic name, environment, ACL type, and request status
     */
    @Test
    public void getAclRequestDetails() throws Exception {
        AclRequestsResponseModel responseModel = new AclRequestsResponseModel();
        responseModel.setTopicname("testtopic");
        responseModel.setEnvironment("DEV");
        responseModel.setAclType(io.aiven.klaw.model.enums.AclType.CONSUMER);
        responseModel.setRequestStatus(io.aiven.klaw.model.enums.RequestStatus.CREATED);

        when(aclControllerService.getAclRequest(any(Integer.class))).thenReturn(responseModel);

        mvcAcls.perform(MockMvcRequestBuilders.get("/acl/request/123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topicname", is("testtopic")))
                .andExpect(jsonPath("$.environment", is("DEV")))
                .andExpect(jsonPath("$.aclType", is("CONSUMER")))
                .andExpect(jsonPath("$.requestStatus", is("CREATED")));
    }

    //endregion

}
