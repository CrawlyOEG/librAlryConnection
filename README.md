librAlryConnection
===================================================

`librAlryConnection` is a library to automate options for libralry.

© 2018 Jorge Galán - OEG-UPM. Available under Apache License 2.0. See [`LICENSE`](LICENSE).

## Features

- Build a model from diferent articles thanks to [librairy](http://librairy.linkeddata.es/) technology.
- Build a space to detect nearby points [librairy](http://librairy.linkeddata.es/) technology.

## Requirements

- You need a persistent internet connection
- Make sure you have your own account on librairy, on sections Topics Discoverer and CRDC-based Vectorial Space
- To interact with the source code, you need the [PDFExtractor](https://github.com/CrawlyOEG/PDFExtractor) library 

## Download

Download a version of the librAlryConnection's jar from our [releases page](../../releases).

## Usage

`librAlryConnection` provides a command line application:

```
$java -jar librAlryConnection.jar --help 
usage: PDFExtractor [-c <mail>] [-f <folder>] [-h] [-k <KeyWord>] [-m
       <TRAINING|VECTOR|COMPLETE>] [-n <Number of PDF's>] [-p <librAlry
       Password>] [-t <Training path>] [-u <librAlry user>] [-v <Vector
       path>]
Mised argument
 -c,--mail <mail>                       Email address to notify the user
                                        of the topic construction
 -f,--folder <folder>                   [REQUIRED] Path to the folder in
                                        which the PDFs are located
 -h,--help                              Indicate how yo use the program.
 -k,--keyword <KeyWord>                 [REQUIRED] Keyword to use in
                                        librAlry
 -m,--mode <TRAINING|VECTOR|COMPLETE>   [REQUIRED] Indicates a mode of use
                                        of the program:
                                        TRAINING: Building a model with
                                        topics
                                        VECTOR: Building points to
                                        indicate whether the PDF belongs
                                        or not
                                        COMPLETE: Make the two previous
                                        ones
 -n,--number <Number of PDF's>          Number of PDF you want to upload
                                        to your model. By default, all
                                        those present in the folder are
                                        used.
 -p,--password <librAlry Password>      [REQUIRED] Password to use in
                                        librAlry
 -t,--training <Training path>          [REQUIRED] URL to build a model
                                        with your topics
 -u,--user <librAlry user>              [REQUIRED] User to use in librAlry
 -v,--vector <Vector path>              [Required in VECTOR mode] URL to
                                        upload your files to build the
                                        points
```


## Building from Source

Clone this repo and run:

```
mvn clean compile assembly:single
```

Then, get your own version of the jar in the project's `target` folder.

<a title="OEG Laboratory" href="http://www.oeg-upm.net/" target="_blank"><img alt="OEG Laboratory" src="http://stars4all.eu/wp-content/uploads/2016/10/OEG.png" width="300" height="300"></a>
<a title="STARS4ALL" href="http://stars4all.eu" target="_blank"><img alt="STARS4ALL" src="http://linkeddata4.dia.fi.upm.es/wordpress-new/wp-content/uploads/2016/12/logo_dark.png" width="220" height="220"></a>