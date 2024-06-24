package myproject;

import com.pulumi.Pulumi;
import com.pulumi.asset.FileAsset;
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
import com.pulumi.azurenative.network.PrivateZone;
import com.pulumi.azurenative.network.PrivateZoneArgs;
import com.pulumi.azurenative.network.Subnet;
import com.pulumi.azurenative.network.SubnetArgs;
import com.pulumi.azurenative.network.VirtualNetwork;
import com.pulumi.azurenative.network.VirtualNetworkArgs;
import com.pulumi.azurenative.network.VirtualNetworkLink;
import com.pulumi.azurenative.network.VirtualNetworkLinkArgs;
import com.pulumi.azurenative.network.inputs.AddressSpaceArgs;
import com.pulumi.azurenative.network.inputs.DelegationArgs;
import com.pulumi.azurenative.network.inputs.ServiceEndpointPropertiesFormatArgs;
import com.pulumi.azurenative.network.inputs.SubResourceArgs;
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

            var primaryStorageKey = getStorageAccountPrimaryKey(
                    resourceGroup.name(),
                    storageAccount.name());

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
                    .serviceEndpoints(ServiceEndpointPropertiesFormatArgs.builder().service("Microsoft.Storage").build())
                    .delegations(DelegationArgs.builder()
                            .name("fs")
                            .serviceName("Microsoft.DBforMySQL/flexibleServers")
                            .actions("Microsoft.Network/virtualNetworks/subnets/join/action")
                            .build())
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
                    .network(NetworkArgs.builder().delegatedSubnetResourceId(subnet.id()).privateDnsZoneResourceId(exampleZone.id()).build())
                    .build());



            var database = new Database("database", DatabaseArgs.builder()
                    .charset("utf8")
                    .collation("utf8_general_ci")
                    .databaseName(nameDB)
                    .resourceGroupName(resourceGroup.name())
                    .serverName(server.name())
                    .build());






        });
    }

    private static Output<String> getStorageAccountPrimaryKey(Output<String> resourceGroupName,
                                                              Output<String> accountName) {
        return StorageFunctions.listStorageAccountKeys(ListStorageAccountKeysArgs.builder()
                                                       .resourceGroupName(resourceGroupName)
                                                       .accountName(accountName)
                                                       .build())
            .applyValue(r -> r.keys().get(0).value())
            .asSecret();
    }
}
