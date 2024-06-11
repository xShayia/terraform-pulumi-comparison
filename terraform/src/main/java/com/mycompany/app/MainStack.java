package com.mycompany.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hashicorp.cdktf.Token;
import com.hashicorp.cdktf.providers.aws.apprunner_service.ApprunnerService;
import com.hashicorp.cdktf.providers.aws.apprunner_service.ApprunnerServiceConfig;
import com.hashicorp.cdktf.providers.aws.apprunner_service.ApprunnerServiceSourceConfiguration;
import com.hashicorp.cdktf.providers.aws.apprunner_service.ApprunnerServiceSourceConfigurationAuthenticationConfiguration;
import com.hashicorp.cdktf.providers.aws.apprunner_service.ApprunnerServiceSourceConfigurationImageRepository;
import com.hashicorp.cdktf.providers.aws.apprunner_service.ApprunnerServiceSourceConfigurationImageRepositoryImageConfiguration;
import com.hashicorp.cdktf.providers.aws.cloudfront_distribution.CloudfrontDistribution;
import com.hashicorp.cdktf.providers.aws.cloudfront_distribution.CloudfrontDistributionConfig;
import com.hashicorp.cdktf.providers.aws.cloudfront_distribution.CloudfrontDistributionDefaultCacheBehavior;
import com.hashicorp.cdktf.providers.aws.cloudfront_distribution.CloudfrontDistributionOrigin;
import com.hashicorp.cdktf.providers.aws.cloudfront_distribution.CloudfrontDistributionRestrictions;
import com.hashicorp.cdktf.providers.aws.cloudfront_distribution.CloudfrontDistributionRestrictionsGeoRestriction;
import com.hashicorp.cdktf.providers.aws.cloudfront_distribution.CloudfrontDistributionViewerCertificate;
import com.hashicorp.cdktf.providers.aws.cloudfront_origin_access_control.CloudfrontOriginAccessControl;
import com.hashicorp.cdktf.providers.aws.cloudfront_origin_access_control.CloudfrontOriginAccessControlConfig;
import com.hashicorp.cdktf.providers.aws.data_aws_db_instance.DataAwsDbInstance;
import com.hashicorp.cdktf.providers.aws.data_aws_ecr_authorization_token.DataAwsEcrAuthorizationToken;
import com.hashicorp.cdktf.providers.aws.data_aws_ecr_authorization_token.DataAwsEcrAuthorizationTokenConfig;
import com.hashicorp.cdktf.providers.aws.data_aws_iam_policy_document.DataAwsIamPolicyDocument;
import com.hashicorp.cdktf.providers.aws.data_aws_iam_policy_document.DataAwsIamPolicyDocumentConfig;
import com.hashicorp.cdktf.providers.aws.data_aws_iam_policy_document.DataAwsIamPolicyDocumentStatement;
import com.hashicorp.cdktf.providers.aws.data_aws_iam_policy_document.DataAwsIamPolicyDocumentStatementCondition;
import com.hashicorp.cdktf.providers.aws.data_aws_iam_policy_document.DataAwsIamPolicyDocumentStatementPrincipals;
import com.hashicorp.cdktf.providers.aws.db_instance.DbInstance;
import com.hashicorp.cdktf.providers.aws.db_instance.DbInstanceConfig;
import com.hashicorp.cdktf.providers.aws.db_subnet_group.DbSubnetGroup;
import com.hashicorp.cdktf.providers.aws.db_subnet_group.DbSubnetGroupConfig;
import com.hashicorp.cdktf.providers.aws.ecr_repository.EcrRepository;
import com.hashicorp.cdktf.providers.aws.ecr_repository.EcrRepositoryConfig;
import com.hashicorp.cdktf.providers.aws.iam_policy_attachment.IamPolicyAttachment;
import com.hashicorp.cdktf.providers.aws.iam_role.IamRole;
import com.hashicorp.cdktf.providers.aws.iam_role.IamRoleConfig;
import com.hashicorp.cdktf.providers.aws.internet_gateway.InternetGateway;
import com.hashicorp.cdktf.providers.aws.internet_gateway.InternetGatewayConfig;
import com.hashicorp.cdktf.providers.aws.provider.AwsProvider;
import com.hashicorp.cdktf.providers.aws.route_table.RouteTable;
import com.hashicorp.cdktf.providers.aws.route_table.RouteTableConfig;
import com.hashicorp.cdktf.providers.aws.route_table.RouteTableRoute;
import com.hashicorp.cdktf.providers.aws.route_table_association.RouteTableAssociation;
import com.hashicorp.cdktf.providers.aws.route_table_association.RouteTableAssociationConfig;
import com.hashicorp.cdktf.providers.aws.s3_bucket.S3Bucket;
import com.hashicorp.cdktf.providers.aws.s3_bucket.S3BucketConfig;
import com.hashicorp.cdktf.providers.aws.s3_bucket_policy.S3BucketPolicy;
import com.hashicorp.cdktf.providers.aws.s3_bucket_policy.S3BucketPolicyConfig;
import com.hashicorp.cdktf.providers.aws.subnet.Subnet;
import com.hashicorp.cdktf.providers.aws.subnet.SubnetConfig;
import com.hashicorp.cdktf.providers.aws.vpc.Vpc;
import com.hashicorp.cdktf.providers.aws.vpc.VpcConfig;
import com.hashicorp.cdktf.providers.docker.image.Image;
import com.hashicorp.cdktf.providers.docker.image.ImageBuild;
import com.hashicorp.cdktf.providers.docker.image.ImageConfig;
import com.hashicorp.cdktf.providers.docker.provider.DockerProviderRegistryAuth;
import com.hashicorp.cdktf.providers.docker.registry_image.RegistryImage;
import com.hashicorp.cdktf.providers.docker.registry_image.RegistryImageConfig;
import software.constructs.Construct;

import com.hashicorp.cdktf.TerraformStack;


import com.hashicorp.cdktf.providers.docker.provider.DockerProvider;

public class MainStack extends TerraformStack {
	public MainStack(final Construct scope, final String id) {
		super(scope, id);

		String s3originId = "S3originId";


		AwsProvider.Builder.create(this, "AWS")
				.region("eu-central-1")
				.build();


		S3Bucket bucket = new S3Bucket(this, "bucket", S3BucketConfig.builder()
				.bucketPrefix("frontend-static")
				.build()
		);

		CloudfrontDistribution cloudfrontDistribution = new CloudfrontDistribution(this, "cloudFront", CloudfrontDistributionConfig.builder()
				.defaultCacheBehavior(
						CloudfrontDistributionDefaultCacheBehavior.builder()
								.allowedMethods(List.of("DELETE",
										"GET",
										"HEAD",
										"OPTIONS",
										"PATCH",
										"POST",
										"PUT"))
								.cachedMethods(List.of("GET", "HEAD"))
								.targetOriginId(s3originId).viewerProtocolPolicy("redirect-to-https")
								.cachePolicyId("4135ea2d-6df8-44a3-9df3-4b5a84be39ad").build()
				)
				.origin(List.of(CloudfrontDistributionOrigin.builder().domainName(bucket.getBucketRegionalDomainName())
						.originAccessControlId(
								new CloudfrontOriginAccessControl(this, "originAccess",
										CloudfrontOriginAccessControlConfig.builder().name("bucketOriginAccessControl")
												.originAccessControlOriginType("s3").signingBehavior("always")
												.signingProtocol("sigv4")
												.build()
								).getId()
						).originId(s3originId).build())).enabled(true)
				.restrictions(CloudfrontDistributionRestrictions.builder()
						.geoRestriction(CloudfrontDistributionRestrictionsGeoRestriction.builder()
								.restrictionType("none").build()).build())
				.viewerCertificate(CloudfrontDistributionViewerCertificate.builder().cloudfrontDefaultCertificate(true)
						.build())

				.build());

		var readOnlyAccess = new DataAwsIamPolicyDocument(this, "readOnlyAcc", DataAwsIamPolicyDocumentConfig.builder()
				.version("2012-10-17").statement(List.of(
						DataAwsIamPolicyDocumentStatement.builder()
								.sid("AllowCloudFrontServicePrincipalReadOnly")
								.effect("Allow")
								.principals(List.of(DataAwsIamPolicyDocumentStatementPrincipals.builder()
										.type("Service")
										.identifiers(List.of("cloudfront.amazonaws.com"))
										.build()))
								.actions(List.of("s3:GetObject"))
								.resources(List.of(bucket.getArn() + "/*"))
								.condition(List.of(DataAwsIamPolicyDocumentStatementCondition.builder()
										.test("StringEquals").variable("AWS:SourceArn")
										.values(List.of(cloudfrontDistribution.getArn())).build()))
								.build()
				)).build());


		new S3BucketPolicy(this, "s3BucketPolicy", S3BucketPolicyConfig.builder().bucket(bucket.getBucket())
				.policy(Token.asString(readOnlyAccess.getJson())).build());


		EcrRepository ecrRepository = new EcrRepository(this, "ecrRepository", EcrRepositoryConfig.builder()
				.name("ecrrepo").imageTagMutability("MUTABLE").build());

		var dataAwsEcrAuthorizationToken = new DataAwsEcrAuthorizationToken(this, "dataAwsEcrAuthorizationToken", DataAwsEcrAuthorizationTokenConfig.builder()
				.build());


		DockerProvider.Builder.create(this, "docker")
				.registryAuth(List.of(DockerProviderRegistryAuth.builder()
						.username(dataAwsEcrAuthorizationToken.getUserName())
						.password(dataAwsEcrAuthorizationToken.getPassword())
						.address(dataAwsEcrAuthorizationToken.getProxyEndpoint())
						.build()))
				.build();

		Image image = new Image(this, "image", ImageConfig
				.builder()
				.name(ecrRepository.getRepositoryUrl())
				.buildAttribute(ImageBuild.builder()
						.context("C:\\Users\\alitu\\IdeaProjects\\terraform-pulumi-comparison\\backend").build()
				)
				.build());


		RegistryImage registryImage = new RegistryImage(this, "registryImage", RegistryImageConfig.builder()
				.name(image.getName())
				.keepRemotely(true)
				.build());


		var appRunIamRole = new DataAwsIamPolicyDocument(this, "readOnlyAccc", DataAwsIamPolicyDocumentConfig.builder()
				.version("2012-10-17").statement(List.of(DataAwsIamPolicyDocumentStatement.builder().effect("Allow")
						.principals(List.of(DataAwsIamPolicyDocumentStatementPrincipals.builder()
								.type("Service")
								.identifiers(List.of("build.apprunner.amazonaws.com"))
								.build()))
						.actions(List.of("sts:AssumeRole")).build()))
				.build()
		);


		IamRole iamRole = new IamRole(this, "iamRole", IamRoleConfig.builder()
				.assumeRolePolicy(Token.asString(appRunIamRole.getJson()))
				.managedPolicyArns(List.of("arn:aws:iam::aws:policy/service-role/AWSAppRunnerServicePolicyForECRAccess"))
				.build());


		ApprunnerService apprunnerService = new ApprunnerService(this, "apprunnerService2", ApprunnerServiceConfig
				.builder()
				.serviceName("terraformService")
				.sourceConfiguration(ApprunnerServiceSourceConfiguration.builder()
						.imageRepository(ApprunnerServiceSourceConfigurationImageRepository.builder()
								.imageConfiguration(ApprunnerServiceSourceConfigurationImageRepositoryImageConfiguration.builder()
										.port("8080").build())
								.imageIdentifier(image.getName() + ":latest")
								.imageRepositoryType("ECR").build())
						.autoDeploymentsEnabled(false)
						.authenticationConfiguration(ApprunnerServiceSourceConfigurationAuthenticationConfiguration.builder()
								.accessRoleArn(iamRole.getArn()).build())
						.build()
				)
				.build());


		//availability zones
		List<String> azs = List.of("eu-central-1a", "eu-central-1b", "eu-central-1c");


		Vpc vpc = new Vpc(this, "vpc", VpcConfig.builder()
				.cidrBlock("10.0.0.0/16")
				.build());

		List<String> public_subnet_cidrs = List.of("10.0.1.0/24", "10.0.2.0/24", "10.0.3.0/24");
		List<String> private_subnet_cidrs = List.of("10.0.4.0/24", "10.0.5.0/24", "10.0.6.0/24");


		//ip distribution to azs
		var cidr_to_azs_public = Map.of(public_subnet_cidrs.get(0), azs.get(0), public_subnet_cidrs.get(1), azs.get(1), public_subnet_cidrs.get(2), azs.get(2));
		var cidr_to_azs_private = Map.of(private_subnet_cidrs.get(0), azs.get(0), private_subnet_cidrs.get(1), azs.get(1), private_subnet_cidrs.get(2), azs.get(2));


		Map<String, Subnet> publicSubnetMap = new HashMap<>();


		public_subnet_cidrs
				.forEach(cidr -> {
					publicSubnetMap.put(cidr, new Subnet(this, "_" + cidr.replace(".", "_")
							.replace("/", "_"), SubnetConfig.builder()
							.cidrBlock(cidr).vpcId(vpc.getId()).availabilityZone(cidr_to_azs_public.get(cidr))
							.build()));
				});
		var private_subnet = private_subnet_cidrs.stream()
				.map(cidr -> new Subnet(this, "_" + cidr.replace(".", "_").replace("/", "_"), SubnetConfig.builder()
						.vpcId(vpc.getId()).cidrBlock(cidr).availabilityZone(cidr_to_azs_private.get(cidr)).build()))
				.toList();


		//conexiune vpc si internet (subnet sa poata accesa internetul)
		InternetGateway internetGateway = new InternetGateway(this, "internetGateway", InternetGatewayConfig.builder()
				.vpcId(vpc.getId()).build());

		RouteTable routeTable = new RouteTable(this, "routeTable", RouteTableConfig.builder().vpcId(vpc.getId())
				.route(List.of(RouteTableRoute.builder().cidrBlock("0.0.0.0/0").gatewayId(internetGateway.getId())
						.build())).build());
		public_subnet_cidrs.forEach(cidr -> new RouteTableAssociation(this, "_" + "_" + cidr.replace(".", "_")
				.replace("/", "_"), RouteTableAssociationConfig.builder().subnetId(publicSubnetMap.get(cidr).getId())
				.routeTableId(routeTable.getId()).build()));


		DbSubnetGroup dbSubnetGroup = new DbSubnetGroup(this, "dbSunetGroup", DbSubnetGroupConfig.builder().name("main")
				.subnetIds(private_subnet.stream().map(Subnet::getId).toList()).build());


		DbInstance dbInstance = new DbInstance(this, "dbInstance", DbInstanceConfig.builder().allocatedStorage(10)
				.dbName("mydb").engine("mysql").engineVersion("8.0").instanceClass("db.t3.micro").username("user")
				.password("useruser").parameterGroupName("default.mysql8.0").skipFinalSnapshot(true)
				.dbSubnetGroupName(dbSubnetGroup.getName()).build());


//        Container.Builder.create(this, "nginxContainer")
//                .image(image.getName())
//                .name("tutorial")
//                .ports(List.of(ContainerPorts.builder().internal(80).external(8000).build()))
//                .build();
	}
}