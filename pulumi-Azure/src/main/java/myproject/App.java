package myproject;

import java.util.Arrays;
import java.util.List;

import com.pulumi.Pulumi;
import com.pulumi.asset.FileAsset;
import com.pulumi.azurenative.app.ContainerApp;
import com.pulumi.azurenative.app.ContainerAppArgs;
import com.pulumi.azurenative.app.ManagedEnvironment;
import com.pulumi.azurenative.app.ManagedEnvironmentArgs;
import com.pulumi.azurenative.app.enums.ManagedServiceIdentityType;
import com.pulumi.azurenative.app.inputs.AppLogsConfigurationArgs;
import com.pulumi.azurenative.app.inputs.ConfigurationArgs;
import com.pulumi.azurenative.app.inputs.ContainerAppProbeArgs;
import com.pulumi.azurenative.app.inputs.ContainerAppProbeHttpGetArgs;
import com.pulumi.azurenative.app.inputs.ContainerArgs;
import com.pulumi.azurenative.app.inputs.CorsPolicyArgs;
import com.pulumi.azurenative.app.inputs.DaprArgs;
import com.pulumi.azurenative.app.inputs.EnvironmentSkuPropertiesArgs;
import com.pulumi.azurenative.app.inputs.EnvironmentVarArgs;
import com.pulumi.azurenative.app.inputs.IngressArgs;
import com.pulumi.azurenative.app.inputs.LogAnalyticsConfigurationArgs;
import com.pulumi.azurenative.app.inputs.ManagedEnvironmentOutboundSettingsArgs;
import com.pulumi.azurenative.app.inputs.ManagedServiceIdentityArgs;
import com.pulumi.azurenative.app.inputs.RegistryCredentialsArgs;
import com.pulumi.azurenative.app.inputs.TemplateArgs;
import com.pulumi.azurenative.app.inputs.VnetConfigurationArgs;
import com.pulumi.azurenative.authorization.RoleAssignment;
import com.pulumi.azurenative.authorization.RoleAssignmentArgs;
import com.pulumi.azurenative.authorization.inputs.GetRoleDefinitionArgs;
import com.pulumi.azurenative.authorization.outputs.GetClientConfigResult;
import com.pulumi.azurenative.authorization.outputs.GetRoleDefinitionResult;
import com.pulumi.azurenative.cdn.Endpoint;
import com.pulumi.azurenative.cdn.EndpointArgs;
import com.pulumi.azurenative.cdn.Origin;
import com.pulumi.azurenative.cdn.OriginArgs;
import com.pulumi.azurenative.cdn.OriginGroup;
import com.pulumi.azurenative.cdn.OriginGroupArgs;
import com.pulumi.azurenative.cdn.Profile;
import com.pulumi.azurenative.cdn.ProfileArgs;
import com.pulumi.azurenative.cdn.Route;
import com.pulumi.azurenative.cdn.RouteArgs;
import com.pulumi.azurenative.cdn.inputs.DeepCreatedOriginArgs;
import com.pulumi.azurenative.cdn.inputs.ResourceReferenceArgs;
import com.pulumi.azurenative.containerregistry.Registry;
import com.pulumi.azurenative.containerregistry.RegistryArgs;
import com.pulumi.azurenative.dbformysql.Database;
import com.pulumi.azurenative.dbformysql.DatabaseArgs;
import com.pulumi.azurenative.dbformysql.Server;
import com.pulumi.azurenative.dbformysql.ServerArgs;
import com.pulumi.azurenative.dbformysql.enums.ServerVersion;
import com.pulumi.azurenative.dbformysql.inputs.BackupArgs;
import com.pulumi.azurenative.dbformysql.inputs.HighAvailabilityArgs;
import com.pulumi.azurenative.dbformysql.inputs.NetworkArgs;
import com.pulumi.azurenative.dbformysql.inputs.SkuArgs;
import com.pulumi.azurenative.dbformysql.inputs.StorageArgs;
import com.pulumi.azurenative.managedidentity.UserAssignedIdentity;
import com.pulumi.azurenative.managedidentity.UserAssignedIdentityArgs;
import com.pulumi.azurenative.network.FrontDoor;
import com.pulumi.azurenative.network.FrontDoorArgs;
import com.pulumi.azurenative.network.PrivateZone;
import com.pulumi.azurenative.network.PrivateZoneArgs;
import com.pulumi.azurenative.network.Subnet;
import com.pulumi.azurenative.network.SubnetArgs;
import com.pulumi.azurenative.network.VirtualNetwork;
import com.pulumi.azurenative.network.VirtualNetworkArgs;
import com.pulumi.azurenative.network.VirtualNetworkLink;
import com.pulumi.azurenative.network.VirtualNetworkLinkArgs;
import com.pulumi.azurenative.network.enums.FrontDoorProtocol;
import com.pulumi.azurenative.network.inputs.AddressSpaceArgs;
import com.pulumi.azurenative.network.inputs.BackendArgs;
import com.pulumi.azurenative.network.inputs.BackendPoolArgs;
import com.pulumi.azurenative.network.inputs.DelegationArgs;
import com.pulumi.azurenative.network.inputs.LoadBalancingSettingsModelArgs;
import com.pulumi.azurenative.network.inputs.RoutingRuleArgs;
import com.pulumi.azurenative.network.inputs.ServiceEndpointPropertiesFormatArgs;
import com.pulumi.azurenative.network.inputs.SubResourceArgs;
import com.pulumi.azurenative.operationalinsights.Workspace;
import com.pulumi.azurenative.operationalinsights.WorkspaceArgs;
import com.pulumi.azurenative.operationalinsights.inputs.GetWorkspaceSharedKeysArgs;
import com.pulumi.azurenative.operationalinsights.inputs.WorkspaceSkuArgs;
import com.pulumi.azurenative.operationalinsights.outputs.GetWorkspaceSharedKeysResult;
import com.pulumi.azurenative.resources.ResourceGroup;
import com.pulumi.azurenative.storage.Blob;
import com.pulumi.azurenative.storage.BlobArgs;
import com.pulumi.azurenative.storage.StorageAccount;
import com.pulumi.azurenative.storage.StorageAccountArgs;
import com.pulumi.azurenative.storage.StorageAccountStaticWebsite;
import com.pulumi.azurenative.storage.StorageAccountStaticWebsiteArgs;
import com.pulumi.azurenative.storage.StorageFunctions;
import com.pulumi.azurenative.storage.enums.Kind;
import com.pulumi.azurenative.storage.enums.SkuName;
import com.pulumi.azurenative.storage.inputs.ListStorageAccountKeysArgs;
import com.pulumi.core.Either;
import com.pulumi.core.Output;
import com.pulumi.deployment.InvokeOptions;
import com.pulumi.azurenative.storage.StorageAccountStaticWebsite;
import com.pulumi.azurenative.storage.StorageAccountStaticWebsiteArgs;
import com.pulumi.azurenative.storage.Blob;
import com.pulumi.azurenative.storage.BlobArgs;
import com.pulumi.azurenative.storage.outputs.EndpointsResponse;
import com.pulumi.asset.FileAsset;
import com.pulumi.docker.Image;
import com.pulumi.docker.ImageArgs;
import com.pulumi.docker.inputs.DockerBuildArgs;
import com.pulumi.resources.CustomResourceOptions;

import static com.pulumi.azurenative.authorization.AuthorizationFunctions.getClientConfig;
import static com.pulumi.azurenative.authorization.AuthorizationFunctions.getRoleDefinition;
import static com.pulumi.azurenative.operationalinsights.OperationalinsightsFunctions.getWorkspaceSharedKeys;

public class App {
	public static void main(String[] args) {
		Pulumi.run(ctx -> {

			String usernameDB = "user";
			String passDB = "useruser";
			String nameDB = "mydb";


			var resourceGroup = new ResourceGroup("resourceGroup");
			var storageAccount = new StorageAccount("sa", StorageAccountArgs.builder()
					.resourceGroupName(resourceGroup.name())
					.sku(com.pulumi.azurenative.storage.inputs.SkuArgs.builder()
							.name(SkuName.Standard_LRS)
							.build())
					.kind(Kind.StorageV2)
					.build());

			var staticWebsite = new StorageAccountStaticWebsite("staticWebsite",
					StorageAccountStaticWebsiteArgs.builder()
							.accountName(storageAccount.name())
							.resourceGroupName(resourceGroup.name())
							.indexDocument("index.html")
							.build());


			var bucketAzure = new Blob("bucketAzure", BlobArgs.builder()
					.resourceGroupName(resourceGroup.name())
					.accountName(storageAccount.name())
					.containerName(staticWebsite.containerName())
					.source(new FileAsset("../frontend/azure.png"))
					.contentType("png")
					.blobName("azure.png")
					.build());

			ctx.export("staticEndpoint", storageAccount.primaryEndpoints()
					.applyValue(EndpointsResponse::web));

			var virtualNetwork = new VirtualNetwork("VPCvirtualNetwork", VirtualNetworkArgs.builder()
					.addressSpace(AddressSpaceArgs.builder()
							.addressPrefixes("10.0.0.0/16")
							.build())
					.flowTimeoutInMinutes(10)
					.resourceGroupName(resourceGroup.name())
					.virtualNetworkName("vpc")
					.build());

			var subnet = new Subnet("subnet", SubnetArgs.builder()
					.addressPrefix("10.0.1.0/24")
					.resourceGroupName(resourceGroup.name())
					.subnetName("subnet")
					.virtualNetworkName(virtualNetwork.name())
					.serviceEndpoints(ServiceEndpointPropertiesFormatArgs.builder().service("Microsoft.Storage")
							.build())
					.delegations(DelegationArgs.builder()
							.name("fs")
							.serviceName("Microsoft.DBforMySQL/flexibleServers")
							.actions("Microsoft.Network/virtualNetworks/subnets/join/action")
							.build())
					.build());

			var subnet2 = new Subnet("subnet2", SubnetArgs.builder()
					.addressPrefix("10.0.4.0/23")
					.resourceGroupName(resourceGroup.name())
					.subnetName("subnet2")
					.virtualNetworkName(virtualNetwork.name())
					.build());


			var exampleZone = new PrivateZone("exampleZone", PrivateZoneArgs.builder()
					.location("Global")
					.privateZoneName("example.mysql.database.azure.com")
					.resourceGroupName(resourceGroup.name())
					.build());

			var exampleZoneVirtualNetworkLink = new VirtualNetworkLink("exampleZoneVirtualNetworkLink", VirtualNetworkLinkArgs.builder()
					.location("Global")
					.privateZoneName(exampleZone.name())
					.registrationEnabled(true)
					.resourceGroupName(resourceGroup.name())
					.virtualNetwork(SubResourceArgs.builder().id(virtualNetwork.id()).build())
					.build());


			var server = new Server("server", ServerArgs.builder()
					.administratorLogin(usernameDB)
					.administratorLoginPassword(passDB)
					.availabilityZone("1")
					.backup(BackupArgs.builder()
							.backupRetentionDays(7)
							.geoRedundantBackup("Disabled")
							.build())
					.createMode("Default")
//                    .highAvailability(HighAvailabilityArgs.builder()
//                            .mode("ZoneRedundant")
//                            .standbyAvailabilityZone("3")
//                            .build())
//                    .location("southeastasia")
					.resourceGroupName(resourceGroup.name())
					.serverName("mysqlserverr3")
					.sku(SkuArgs.builder()
							.name("Standard_B1ms")
							.tier("Burstable")
							.build())
					.storage(StorageArgs.builder()
							.autoGrow("Disabled")
							.iops(600)
							.storageSizeGB(20)
							.build())
					.version(ServerVersion._8_0_21)
					.network(NetworkArgs.builder().delegatedSubnetResourceId(subnet.id())
							.privateDnsZoneResourceId(exampleZone.id()).build())
					.build());


			var database = new Database("database", DatabaseArgs.builder()
					.charset("utf8")
					.collation("utf8_general_ci")
					.databaseName(nameDB)
					.resourceGroupName(resourceGroup.name())
					.serverName(server.name())
					.build());


			var registry = new Registry("registry", RegistryArgs.builder()
					.adminUserEnabled(true)
					.registryName("alepulumitest123")
					.resourceGroupName(resourceGroup.name())
					.sku(com.pulumi.azurenative.containerregistry.inputs.SkuArgs.builder()
							.name("Standard")
							.build())
					.build());

			var image = new Image("image_pulumi", ImageArgs.builder() //build and push image
					.build(DockerBuildArgs.builder()
							.context("..\\backend\\")
							.dockerfile("..\\backend\\Dockerfile")
							.platform("linux/amd64")
							.build())
					.imageName(registry.name().applyValue(name -> name + ".azurecr.io/backend"))
					.build());

			var workspace = new Workspace("workspace", WorkspaceArgs.builder()
					.resourceGroupName(resourceGroup.name())
					.retentionInDays(30)
					.sku(WorkspaceSkuArgs.builder()
							.name("PerGB2018")
							.build())
					.workspaceName("pulumiworkspace")
					.build());

			var managedEnvironment = new ManagedEnvironment("managedEnvironment", ManagedEnvironmentArgs.builder()
					.appLogsConfiguration(AppLogsConfigurationArgs.builder()
							.destination("log-analytics")
							.logAnalyticsConfiguration(LogAnalyticsConfigurationArgs.builder()
									.customerId(workspace.customerId())
									.sharedKey(
											getWorkspaceSharedKeys(GetWorkspaceSharedKeysArgs.builder()
													.resourceGroupName(resourceGroup.name())
													.workspaceName(workspace.name())
													.build()).applyValue(result -> result.primarySharedKey()
													.orElseThrow())
									)
									.build())
							.build())
					.environmentName("pulumi")
					.resourceGroupName(resourceGroup.name())
					.sku(EnvironmentSkuPropertiesArgs.builder()
							.name("Consumption")
							.build())
					.vnetConfiguration(VnetConfigurationArgs.builder()
							.infrastructureSubnetId(subnet2.id())
							.build())
					.zoneRedundant(false)
					.build());

			var userAssignedIdentity = new UserAssignedIdentity("userAssignedIdentity", UserAssignedIdentityArgs.builder()
					.resourceGroupName(resourceGroup.name())
					.resourceName("resourceName")
					.build());

			var roleAssignment = new RoleAssignment("roleAssignment", RoleAssignmentArgs.builder()
					.principalId(userAssignedIdentity.principalId())
					.roleDefinitionId("/providers/Microsoft.Authorization/roleDefinitions/7f951dda-4ed3-4680-a7ca-43fe172d538d")
					.scope(registry.id())
					.principalType("ServicePrincipal")
					.build());

			var identity = Output.all(getClientConfig().applyValue(GetClientConfigResult::subscriptionId), resourceGroup.name(), userAssignedIdentity.name())
					.applyValue(values -> "/subscriptions/" + values.get(0) + "/resourcegroups/" + values.get(1) + "/providers/Microsoft.ManagedIdentity/userAssignedIdentities/" + values.get(2));
			var containerApp = new ContainerApp("containerApp", ContainerAppArgs.builder()
					.identity(ManagedServiceIdentityArgs.builder()
							.type(ManagedServiceIdentityType.UserAssigned)
							.userAssignedIdentities(Output.all(identity))
							.build())
					.configuration(ConfigurationArgs.builder()
//							.dapr(DaprArgs.builder()
//									.appPort(8080)
//									.appProtocol("http")
//									.enableApiLogging(true)
//									.enabled(true)
//									.httpMaxRequestSize(10)
//									.httpReadBufferSize(30)
//									.logLevel("debug")
//									.build())
							.ingress(IngressArgs.builder()
									.clientCertificateMode("ignore")
									.external(true)
									.targetPort(8080)
//                                    .traffic(TrafficWeightArgs.builder()
//                                            .label("production")
//                                            .revisionName("testcontainerapp0-ab1234")
//                                            .weight(100)
//                                            .build())
									.build())
							.maxInactiveRevisions(10)
							.registries(RegistryCredentialsArgs.builder()
									.identity(
											identity
									).server(registry.loginServer()).build())
							.build())
					.containerAppName("testcontainerapp0")
					.environmentId(managedEnvironment.id())
					.resourceGroupName(resourceGroup.name())
					.template(TemplateArgs.builder()
							.containers(ContainerArgs.builder()
									.image(image.repoDigest())
									.name("testcontainerapp0")
									.probes(ContainerAppProbeArgs.builder()
											.httpGet(ContainerAppProbeHttpGetArgs.builder()
													.path("/")
													.port(8080)
													.build())
											.initialDelaySeconds(3)
											.periodSeconds(3)
											.type("Liveness")
											.build())
									.env(
											Output.all(
													Output.all(server.name(), exampleZone.name()).applyValue(names ->
															EnvironmentVarArgs.builder()
																	.name("MYSQL_HOST")
																	.value(names.get(0) + "." + names.get(1))
																	.build()
													),
													Output.of(EnvironmentVarArgs.builder()
															.name("MYSQL_USER")
															.value(usernameDB)
															.build()),
													Output.of(EnvironmentVarArgs.builder()
															.name("MYSQL_PASS")
															.value(passDB)
															.build()),
													Output.of(EnvironmentVarArgs.builder()
															.name("MYSQL_DB")
															.value(nameDB)
															.build())
											)
									)
									.build())
//                            .scale(ScaleArgs.builder()
//                                    .maxReplicas(5)
//                                    .minReplicas(1)
//                                    .rules(ScaleRuleArgs.builder()
//                                            .custom(CustomScaleRuleArgs.builder()
//                                                    .metadata(Map.of("concurrentRequests", "50"))
//                                                    .type("http")
//                                                    .build())
//                                            .name("httpscalingrule")
//                                            .build())
//                                    .build())
							.build())
					.workloadProfileType("GeneralPurpose")
					.build(), CustomResourceOptions.builder().dependsOn(roleAssignment).build());

			// error: Code="BadRequest" Message="Free Trial and Student account is forbidden for Azure Frontdoor resources."
//			// https://www.pulumi.com/ai/conversations/e5a8ddbf-c7f2-4159-bf3f-16d19e16f050
//			var frontdoorProfile = new Profile("example-frontdoor-profile", ProfileArgs.builder()
//					.resourceGroupName(resourceGroup.name())
//					.sku(com.pulumi.azurenative.cdn.inputs.SkuArgs.builder().name("Standard_AzureFrontDoor").build())
//					.build());


//			var frontdoorOriginGroup = new OriginGroup("example-origin-group", OriginGroupArgs.builder()
//					.resourceGroupName(resourceGroup.name())
//					.profileName(frontdoorProfile.name())
//					.build());

//			var frontdoorEndpoint = new Endpoint("example-frontdoor-endpoint", EndpointArgs.builder()
//					.resourceGroupName(resourceGroup.name())
//					.profileName(frontdoorProfile.name())
//					.origins(DeepCreatedOriginArgs.builder()
//							.name("containerapp")
//							.hostName(
//									containerApp.configuration()
//											.applyValue(config -> config.orElseThrow().ingress().orElseThrow().fqdn())
//
//							).httpPort(80)
//							.httpsPort(443)
//							.build())
//					.build());


//			var frontdoorFrontendRoute = new Route("example-frontend-route", RouteArgs.builder()
//					.resourceGroupName(resourceGroup.name())
//					.profileName(frontdoorProfile.name())
//					.endpointName(frontdoorEndpoint.name())
//					.originGroup(ResourceReferenceArgs.builder().id(frontdoorOrigin.id()).build())
//					.patternsToMatch(List.of("/*"))
//					.build());

//			ctx.export("accessUrl", frontdoorEndpoint.hostName()
//					.applyValue(endpoint -> String.format("https://%s", endpoint)));

		});
	}
}
