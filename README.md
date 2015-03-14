# ocrtest

Project uses Akka for multi-threaded environment, <br />
im4java as a wrapper for ImageMagick for image pre-processing, and <br /> 
tess4j as a wrapper for tesseract for OCR. <br /> 

Using tesseract with tess4J: <br />
<br />
1) sudo apt-get install tesseract-ocr <br />
   or <br />
   download manually <br />
2) download tess4j from http://sourceforge.net/projects/tess4j/ <br />
3) extract tess4j <br />
4) copy tess4j.jar, jai_imageio.jar and jna.jar from extracted tess4j to Project's lib folder <br />
5) copy tessdata from /usr/share/tesseract-ocr to Project's root folder