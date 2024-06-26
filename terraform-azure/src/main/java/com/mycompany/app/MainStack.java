package com.mycompany.app;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Blob;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.hashicorp.cdktf.AssetType;
import com.hashicorp.cdktf.TerraformAsset;
import com.hashicorp.cdktf.TerraformAssetConfig;
import com.hashicorp.cdktf.TerraformResourceLifecycle;
import com.hashicorp.cdktf.providers.azurerm.container_app.ContainerApp;
import com.hashicorp.cdktf.providers.azurerm.container_app.ContainerAppConfig;
import com.hashicorp.cdktf.providers.azurerm.container_app.ContainerAppIdentity;
import com.hashicorp.cdktf.providers.azurerm.container_app.ContainerAppIngress;
import com.hashicorp.cdktf.providers.azurerm.container_app.ContainerAppIngressTrafficWeight;
import com.hashicorp.cdktf.providers.azurerm.container_app.ContainerAppRegistry;
import com.hashicorp.cdktf.providers.azurerm.container_app.ContainerAppTemplate;
import com.hashicorp.cdktf.providers.azurerm.container_app.ContainerAppTemplateContainer;
import com.hashicorp.cdktf.providers.azurerm.container_app.ContainerAppTemplateContainerEnv;
import com.hashicorp.cdktf.providers.azurerm.container_app.ContainerAppTemplateContainerLivenessProbe;
import com.hashicorp.cdktf.providers.azurerm.container_app_environment.ContainerAppEnvironment;
import com.hashicorp.cdktf.providers.azurerm.container_app_environment.ContainerAppEnvironmentConfig;
import com.hashicorp.cdktf.providers.azurerm.container_app_environment.ContainerAppEnvironmentWorkloadProfile;
import com.hashicorp.cdktf.providers.azurerm.container_registry.ContainerRegistry;
import com.hashicorp.cdktf.providers.azurerm.container_registry.ContainerRegistryConfig;
import com.hashicorp.cdktf.providers.azurerm.log_analytics_workspace.LogAnalyticsWorkspace;
import com.hashicorp.cdktf.providers.azurerm.log_analytics_workspace.LogAnalyticsWorkspaceConfig;
import com.hashicorp.cdktf.providers.azurerm.mysql_flexible_database.MysqlFlexibleDatabase;
import com.hashicorp.cdktf.providers.azurerm.mysql_flexible_database.MysqlFlexibleDatabaseConfig;
import com.hashicorp.cdktf.providers.azurerm.mysql_flexible_server.MysqlFlexibleServer;
import com.hashicorp.cdktf.providers.azurerm.mysql_flexible_server.MysqlFlexibleServerConfig;
import com.hashicorp.cdktf.providers.azurerm.mysql_flexible_server.MysqlFlexibleServerStorage;
import com.hashicorp.cdktf.providers.azurerm.private_dns_zone.PrivateDnsZone;
import com.hashicorp.cdktf.providers.azurerm.private_dns_zone.PrivateDnsZoneConfig;
import com.hashicorp.cdktf.providers.azurerm.private_dns_zone_virtual_network_link.PrivateDnsZoneVirtualNetworkLink;
import com.hashicorp.cdktf.providers.azurerm.private_dns_zone_virtual_network_link.PrivateDnsZoneVirtualNetworkLinkConfig;
import com.hashicorp.cdktf.providers.azurerm.provider.AzurermProvider;
import com.hashicorp.cdktf.providers.azurerm.provider.AzurermProviderConfig;
import com.hashicorp.cdktf.providers.azurerm.provider.AzurermProviderFeatures;
import com.hashicorp.cdktf.providers.azurerm.resource_group.ResourceGroup;
import com.hashicorp.cdktf.providers.azurerm.resource_group.ResourceGroupConfig;
import com.hashicorp.cdktf.providers.azurerm.role_assignment.RoleAssignment;
import com.hashicorp.cdktf.providers.azurerm.role_assignment.RoleAssignmentConfig;
import com.hashicorp.cdktf.providers.azurerm.storage_account.StorageAccount;
import com.hashicorp.cdktf.providers.azurerm.storage_account.StorageAccountConfig;
import com.hashicorp.cdktf.providers.azurerm.storage_account.StorageAccountStaticWebsite;
import com.hashicorp.cdktf.providers.azurerm.storage_blob.StorageBlob;
import com.hashicorp.cdktf.providers.azurerm.storage_blob.StorageBlobConfig;
import com.hashicorp.cdktf.providers.azurerm.subnet.Subnet;
import com.hashicorp.cdktf.providers.azurerm.subnet.SubnetConfig;
import com.hashicorp.cdktf.providers.azurerm.subnet.SubnetDelegation;
import com.hashicorp.cdktf.providers.azurerm.subnet.SubnetDelegationServiceDelegation;
import com.hashicorp.cdktf.providers.azurerm.user_assigned_identity.UserAssignedIdentity;
import com.hashicorp.cdktf.providers.azurerm.user_assigned_identity.UserAssignedIdentityConfig;
import com.hashicorp.cdktf.providers.azurerm.virtual_network.VirtualNetwork;
import com.hashicorp.cdktf.providers.azurerm.virtual_network.VirtualNetworkConfig;
import com.hashicorp.cdktf.providers.docker.image.Image;
import com.hashicorp.cdktf.providers.docker.image.ImageBuild;
import com.hashicorp.cdktf.providers.docker.image.ImageConfig;
import com.hashicorp.cdktf.providers.docker.provider.DockerProvider;
import com.hashicorp.cdktf.providers.docker.provider.DockerProviderRegistryAuth;
import com.hashicorp.cdktf.providers.docker.registry_image.RegistryImage;
import com.hashicorp.cdktf.providers.docker.registry_image.RegistryImageConfig;
import software.constructs.Construct;

import com.hashicorp.cdktf.TerraformStack;

public class MainStack extends TerraformStack {
	public MainStack(final Construct scope, final String id) {
		super(scope, id);

		String usernameDB = "user";
		String passDB = "Useruser123@";
		String nameDB = "mydb";
		String subId = "938db138-fb73-4867-a64c-6ac4d5951e88";
		String region = "northeurope";

		var provider = new AzurermProvider(this, "azure", AzurermProviderConfig.builder()
				.features(AzurermProviderFeatures.builder().build())
				.subscriptionId(subId)
				.build());

		// https://learn.microsoft.com/en-us/azure/storage/blobs/storage-quickstart-static-website-terraform?tabs=azure-cli
		var resourceGroup = new ResourceGroup(this, "resourceGroup", ResourceGroupConfig.builder()
				.location(region)
				.name("resourcegrouptf")
				.build()
		);
		var storageAccount = new StorageAccount(this, "sa", StorageAccountConfig.builder()
				.name("terraformsacale24251")
				.resourceGroupName(resourceGroup.getName())
				.staticWebsite(StorageAccountStaticWebsite.builder()
						.indexDocument("index.html")
						.build())
				.accountReplicationType("LRS")
				.accountTier("Standard")
				.location(region)
				.build());

		try {
			var files = Files.list(Paths.get("C:\\Users\\alitu\\IdeaProjects\\terraform-pulumi-comparison\\frontend\\dist\\demo-app\\browser"))
					.filter(file -> !Files.isDirectory(file))
					.toList();
			files.forEach(file -> {
				var asset = new TerraformAsset(this, file.getFileName()
						.toString() + "asset", TerraformAssetConfig.builder()
						.path(file.toString())
						.type(AssetType.FILE)
						.build()
				);
				var contentType = "";
				try {
					contentType = Files.probeContentType(file);
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
				new StorageBlob(this, file.getFileName().toString() + "s3File", StorageBlobConfig.builder()
						.storageAccountName(storageAccount.getName())
						.storageContainerName("$web")
						.source(asset.getPath())
						.name(file.getFileName().toString())
						.contentType(contentType)
						.type("Block")
						.build()
				);
			});
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		var virtualNetwork = new VirtualNetwork(this, "VPCvirtualNetwork", VirtualNetworkConfig.builder()
				.addressSpace(List.of("10.0.0.0/16"))
				.flowTimeoutInMinutes(10)
				.resourceGroupName(resourceGroup.getName())
				.name("vpc2")
				.location(region)
				.build());

		var subnet = new Subnet(this, "subnet", SubnetConfig.builder()
				.addressPrefixes(List.of("10.0.1.0/24"))
				.resourceGroupName(resourceGroup.getName())
				.name("subnet")
				.virtualNetworkName(virtualNetwork.getName())
				.serviceEndpoints(List.of("Microsoft.Storage"))
				.delegation(List.of(
						SubnetDelegation.builder()
								.name("fs")
								.serviceDelegation(SubnetDelegationServiceDelegation.builder()
										.name("Microsoft.DBforMySQL/flexibleServers")
										.actions(List.of("Microsoft.Network/virtualNetworks/subnets/join/action"))
										.build())
								.build()
				))
				.build());

		var subnet2 = new Subnet(this, "subnet2", SubnetConfig.builder()
				.addressPrefixes(List.of("10.0.4.0/23"))
				.resourceGroupName(resourceGroup.getName())
				.name("subnet2")
				.virtualNetworkName(virtualNetwork.getName())
				.delegation(List.of(
						SubnetDelegation.builder()
								.name("app")
								.serviceDelegation(SubnetDelegationServiceDelegation.builder()
										.name("Microsoft.App/environments")
										.actions(List.of("Microsoft.Network/virtualNetworks/subnets/join/action"))
										.build())
								.build()
				))
				.build());

		var exampleZone = new PrivateDnsZone(this, "exampleZone", PrivateDnsZoneConfig.builder()
				.name("example2.mysql.database.azure.com")
				.resourceGroupName(resourceGroup.getName())
				.build());

		var exampleZoneVirtualNetworkLink = new PrivateDnsZoneVirtualNetworkLink(this, "exampleZoneVirtualNetworkLink", PrivateDnsZoneVirtualNetworkLinkConfig.builder()
				.name("zonevirtualnetwprklink")
				.privateDnsZoneName(exampleZone.getName())
				.registrationEnabled(true)
				.resourceGroupName(resourceGroup.getName())
				.virtualNetworkId(virtualNetwork.getId())
				.build());

		var server = new MysqlFlexibleServer(this, "server", MysqlFlexibleServerConfig.builder()
				.name("mysqlservertf6")
				.administratorLogin(usernameDB)
				.administratorPassword(passDB)
				.resourceGroupName(resourceGroup.getName())
				.location(region)
				.delegatedSubnetId(subnet.getId())
				.privateDnsZoneId(exampleZone.getId())
				.skuName("B_Standard_B1ms")
				.version("8.0.21")
				.backupRetentionDays(7)
				.storage(MysqlFlexibleServerStorage.builder().sizeGb(20).autoGrowEnabled(false).iops(600).build())
				.lifecycle(TerraformResourceLifecycle.builder()
						.ignoreChanges(List.of("zone"))
						.build())
				.build());

		var database = new MysqlFlexibleDatabase(this, "database", MysqlFlexibleDatabaseConfig.builder()
				.charset("utf8mb3")
				.collation("utf8mb3_general_ci")
				.name(nameDB)
				.resourceGroupName(resourceGroup.getName())
				.serverName(server.getName())
				.build());

		var registry = new ContainerRegistry(this, "containerregistry", ContainerRegistryConfig.builder()
				.adminEnabled(true)
				.resourceGroupName(resourceGroup.getName())
				.name("alepulumitest12345")
				.sku("Standard")
				.location(region)
				.build());

		DockerProvider.Builder.create(this, "docker")
				.registryAuth(List.of(
						DockerProviderRegistryAuth.builder()
								.address(registry.getLoginServer())
								.username(registry.getAdminUsername())
								.password(registry.getAdminPassword())
								.build()
				))
				.build();

		Image image = new Image(this, "image", ImageConfig
				.builder()
				.name(registry.getLoginServer() + "/backend")
				.buildAttribute(ImageBuild.builder()
						.context("C:\\Users\\alitu\\IdeaProjects\\terraform-pulumi-comparison\\backend").build()
				)
				.triggers(Map.of("1", UUID.randomUUID().toString()))
				.build());


		RegistryImage registryImage = new RegistryImage(this, "registryImage", RegistryImageConfig.builder()
				.name(image.getName())
				.keepRemotely(true)
				.triggers(Map.of("1", image.getRepoDigest()))
				.build());

		var workspace = new LogAnalyticsWorkspace(this, "workspace", LogAnalyticsWorkspaceConfig.builder()
				.resourceGroupName(resourceGroup.getName())
				.retentionInDays(30)
				.sku("PerGB2018")
				.name("tfworkspace")
				.location(region)
				.build());

		var managedEnvironment = new ContainerAppEnvironment(this, "appenv", ContainerAppEnvironmentConfig.builder()
				.name("tfenv")
				.location(region)
				.resourceGroupName(resourceGroup.getName())
				.logAnalyticsWorkspaceId(workspace.getId())
				.infrastructureSubnetId(subnet2.getId())
				.zoneRedundancyEnabled(false)
				.workloadProfile(
						List.of(
								ContainerAppEnvironmentWorkloadProfile.builder()
										.name("Consumption")
										.workloadProfileType("Consumption")
										.build()
						)
				)
				.build());

		var userAssignedIdentity = new UserAssignedIdentity(this, "user", UserAssignedIdentityConfig.builder()
				.resourceGroupName(resourceGroup.getName())
				.name("userIdentityTf")
				.location(region)
				.build());

		var roleAssignemnt = new RoleAssignment(this, "role", RoleAssignmentConfig.builder()
				.principalId(userAssignedIdentity.getPrincipalId())
				.roleDefinitionId("/providers/Microsoft.Authorization/roleDefinitions/7f951dda-4ed3-4680-a7ca-43fe172d538d")
				.scope(registry.getId())
				.principalType("ServicePrincipal")
				.build());

		var app = new ContainerApp(this, "app", ContainerAppConfig.builder()
				.name("containerapptf")
				.identity(ContainerAppIdentity.builder().identityIds(List.of(userAssignedIdentity.getId()))
						.type("UserAssigned").build())
				.containerAppEnvironmentId(managedEnvironment.getId())
				.ingress(ContainerAppIngress.builder()
						.targetPort(8080)
						.externalEnabled(true)
						.trafficWeight(List.of(
								ContainerAppIngressTrafficWeight.builder()
										.percentage(100)
										.latestRevision(true)
										.label("latest")
										.build()
						))
						.build())
				.registry(
						List.of(
								ContainerAppRegistry.builder()
										.identity(userAssignedIdentity.getId())
										.server(registry.getLoginServer())
										.build()
						)
				)
				.template(ContainerAppTemplate.builder()
						.container(
								List.of(
										ContainerAppTemplateContainer.builder()
												.image(image.getName() + "@" + registryImage.getSha256Digest())
												.name("backend")
												.livenessProbe(List.of(
														ContainerAppTemplateContainerLivenessProbe.builder()
																.path("/")
																.port(8080)
																.initialDelay(3)
																.intervalSeconds(3)
																.transport("HTTP")
																.build()
												))
												.env(List.of(
														ContainerAppTemplateContainerEnv.builder()
																.name("MYSQL_HOST")
																.value(server.getName() + "." + exampleZone.getName())
																.build(),
														ContainerAppTemplateContainerEnv.builder()
																.name("MYSQL_USER")
																.value(usernameDB)
																.build(),
														ContainerAppTemplateContainerEnv.builder()
																.name("MYSQL_PASS")
																.value(passDB)
																.build(),
														ContainerAppTemplateContainerEnv.builder()
																.name("MYSQL_DB")
																.value(nameDB)
																.build()
												))
												.cpu(0.25)
												.memory("0.5Gi")
												.build()
								)
						)
						.build())
				.workloadProfileName("Consumption")
				.resourceGroupName(resourceGroup.getName())
				.revisionMode("Single")
				.build());
	}
}