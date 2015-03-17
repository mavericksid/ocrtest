# ocrtest

Project uses Akka for multi-threaded environment, <br />
im4java as a wrapper for ImageMagick for image pre-processing, and <br /> 
tess4j as a wrapper for tesseract for OCR. <br /> 

<br /> 
Running project: <br /> 
1) Use sbt run to run the project <br /> 
2) Images to convert are located in src/main/resources/images <br /> 
3) Pre processed images will go in src/main/resources/tmp/{IMAGE_NAME} <br /> 
4) OCR output will go in src/main/resources/output/{IMAGE_NAME} <br /> 
