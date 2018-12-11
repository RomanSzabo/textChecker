package textChecker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.similarity.JaccardSimilarity;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;

public class TextSimiliarityChecker {

	private static final String DELIMETER = "/";
	private static final String FOLDER_NAME = "textFiles/";
	private static final String BUCKET_NAME = "elasticbeanstalk-us-east-2-092644405248";
	private static final String AWS_SECRET_KEY = "OpQnW9/o14+QAPEofXePqgzslmT6ZzYS3AmrXmw0";
	private static final String AWS_ACCESS_KEY = "AKIAJ2IQJHDPBLHYVPPA";

	// ALgorithm used to compare two strings
	private JaccardSimilarity algorithm;

	// Access credentials
	private AWSCredentials credentials;

	// AWS client used for connecting to S3 storage
	private AmazonS3 client;

	TransferManager transferManager;

	public TextSimiliarityChecker() {
		algorithm = new JaccardSimilarity();
		credentials = new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY);
		client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
				.withRegion(Regions.US_EAST_2).build();
		transferManager = TransferManagerBuilder.standard().withS3Client(client).build();
	}

	public Pair<Double, String> checkTwoTexts(String textToCheck) throws IOException {
		// Get all objects stored in the "textFiles" folder
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(BUCKET_NAME)
				.withPrefix(FOLDER_NAME).withDelimiter(DELIMETER);
		ObjectListing objects = transferManager.getAmazonS3Client().listObjects(listObjectsRequest);

		List<String> fileNames = new ArrayList<String>();

		for (S3ObjectSummary s : objects.getObjectSummaries()) {
			fileNames.add(s.getKey());
		}

		// Remove the first entry which is the folder
		fileNames.remove(0);

		Map<Double, String> scoresWithNames = new HashMap<Double, String>();

		for (String fileName : fileNames) {
			S3Object object = client.getObject(BUCKET_NAME, fileName);
			S3ObjectInputStream inputStream = object.getObjectContent();
			String textContent = com.amazonaws.util.IOUtils.toString(inputStream);
			scoresWithNames.put(getSimiliariryScore(textToCheck, textContent), fileName);
		}

		// TODO What happens when 2 files have same score
		Double maxScore = Collections.max(scoresWithNames.keySet());
		String mostSimiliarFile = scoresWithNames.get(maxScore);
		System.out.println("Most similiar file was: " + mostSimiliarFile + " with score: " + maxScore);
		return new ImmutablePair<Double, String>(maxScore, mostSimiliarFile);

	}

	private double getSimiliariryScore(String text1, String text2) {
		return algorithm.apply(text1, text2);
	}

}
