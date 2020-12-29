# latex-compile-server
A webserver, running in a Docker container, that can receive and compile [LaTeX documents](http://en.wikipedia.org/wiki/LaTeX). The server accepts zip-files sent to it via Rest Post-calls, and it unzips the given files, compiles them with `xelatex` and sends the resulting pdf back.

[pgfornament](https://ctan.org/pkg/pgfornament) by Alain Matthes et al. is preinstalled and ready to be used in the input documents.

## Running the Docker container
Build and run it with Docker with two simple commands. Note that this will download at least 2-3 GB of data, and the resulting image will be at least 5 GB large!

`docker build -t latex-compile-server .`

`docker run -p 58404:58404 latex-compile-server`

## Example usage
There is a personal invitation sample in the `example` directory, which will produce a document typesetted with pgfornament. The layout and "main document" is in `example/invite.tex`, and the personal information is in `example/personalinfo.tex`.

To compile it, run the Docker container. Compress the two files to a zip: `zip -rj doc.zip example`. Then run `curl -X POST -H "Content-Type: multipart/form-data" -F "mainFile=invite" -F "data=@doc.zip" http://localhost:58404/compile --output invite.pdf`

In the curl command, we have specified a few things:
* `-F "mainFile=invite"` - The main tex file to compile is called `invite.tex`, so we here specify the name of that file (note that we specify "invite", not "invite.tex" in the curl command)
* `F "data=@doc.zip"` - The zip of the documents is called `doc.zip`, so we specify that here
* `--output invite.pdf` - latex-compile-server sends a pdf back, and we here specify the name and location of where to save it.

![Image of example](https://github.com/sjoblomj/latex-compile-server/example/invite.png "PDF from example")

## What this project is
* Convenient - no need to clutter your file system with LaTeX packages. Just call a Rest service.
* Distributable - you can run this on any computer with Docker installed.

## What this project is not
* Small - this will download at least 2-3 GB, and the container will take around 5 GB of disk size.
* Secure - this is intended to be run locally, with trusted data. No security measures whatsoever has been added.
* User friendly - if a file fails to compile, you won't get nice error messages. The easiest way then is to log into the container using `docker logs`, and view the log file for yourself.
