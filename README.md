# ocrtest

Project uses Akka for multi-threaded environment, <br />
im4java as a wrapper for ImageMagick for image pre-processing, and <br /> 
tess4j as a wrapper for tesseract for OCR. <br /> 

<br /> 
Running project: <br /> 
1) Use sbt run to run the project <br /> 
2) Images to convert are located in src/main/resources <br /> 
3) Pre processed images will go in src/main/resources/tmp <br /> 
4) OCR out will go in src/main/resources/output <br /> 

<br />
Using tesseract with tess4J: <br />
1) sudo apt-get install tesseract-ocr <br />
   or <br />
   download manually <br />
2) download tess4j from http://sourceforge.net/projects/tess4j/ <br />
3) extract tess4j <br />
4) copy tess4j.jar, jai_imageio.jar and jna.jar from extracted tess4j to Project's lib folder <br />
5) copy tessdata from /usr/share/tesseract-ocr to Project's root folder <br /> 