package com.mycompany.app;

import java.util.List;

import com.hashicorp.cdktf.Token;
import com.hashicorp.cdktf.providers.aws.cloudfront_distribution.CloudfrontDistribution;
import com.hashicorp.cdktf.providers.aws.cloudfront_distribution.CloudfrontDistributionConfig;
import com.hashicorp.cdktf.providers.aws.cloudfront_distribution.CloudfrontDistributionDefaultCacheBehavior;
import com.hashicorp.cdktf.providers.aws.cloudfront_distribution.CloudfrontDistributionOrigin;
import com.hashicorp.cdktf.providers.aws.cloudfront_distribution.CloudfrontDistributionRestrictions;
import com.hashicorp.cdktf.providers.aws.cloudfront_distribution.CloudfrontDistributionRestrictionsGeoRestriction;
import com.hashicorp.cdktf.providers.aws.cloudfront_distribution.CloudfrontDistributionViewerCertificate;
import com.hashicorp.cdktf.providers.aws.cloudfront_origin_access_control.CloudfrontOriginAccessControl;
import com.hashicorp.cdktf.providers.aws.cloudfront_origin_access_control.CloudfrontOriginAccessControlConfig;
import com.hashicorp.cdktf.providers.aws.data_aws_iam_policy_document.DataAwsIamPolicyDocument;
import com.hashicorp.cdktf.providers.aws.data_aws_iam_policy_document.DataAwsIamPolicyDocumentConfig;
import com.hashicorp.cdktf.providers.aws.data_aws_iam_policy_document.DataAwsIamPolicyDocumentStatement;
import com.hashicorp.cdktf.providers.aws.data_aws_iam_policy_document.DataAwsIamPolicyDocumentStatementCondition;
import com.hashicorp.cdktf.providers.aws.data_aws_iam_policy_document.DataAwsIamPolicyDocumentStatementPrincipals;
import com.hashicorp.cdktf.providers.aws.provider.AwsProvider;
import com.hashicorp.cdktf.providers.aws.s3_bucket.S3Bucket;
import com.hashicorp.cdktf.providers.aws.s3_bucket.S3BucketConfig;
import com.hashicorp.cdktf.providers.aws.s3_bucket_policy.S3BucketPolicy;
import com.hashicorp.cdktf.providers.aws.s3_bucket_policy.S3BucketPolicyConfig;
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

		DockerProvider.Builder.create(this, "docker")
				.build();
//        Image image = Image.Builder.create(this, "nginxImage")
//                .name("nginx:latest")
//                .keepLocally(false)
//                .build();
//        Container.Builder.create(this, "nginxContainer")
//                .image(image.getName())
//                .name("tutorial")
//                .ports(List.of(ContainerPorts.builder().internal(80).external(8000).build()))
//                .build();
	}
}