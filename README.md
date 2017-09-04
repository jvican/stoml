# TOML parser for Scala [![Build Status](https://travis-ci.org/jvican/stoml.svg)](https://travis-ci.org/jvican/stoml)
  
[TOML](https://github.com/toml-lang/toml#user-content-comment) is a 
minimal configuration file format that shines because of its simplicity.

This is a clean-room parser implementation of the TOML spec that allows you to
quickly parse any TOML file and get great error reporting when it fails.

Forget YAML, go TOML.

## Import in your project
```scala
"me.vican.jorge" %% "stoml" % "0.3"
resolvers += Some(Resolver.bintrayRepo("jvican", "releases"))
```
  
## Acknowledgements
Built on top of [Fastparse](https://github.com/lihaoyi/fastparse) (which means fast parsing) and [Scalacheck](https://github.com/rickynils/scalacheck) to automatically check any combination of valid TOML elements.
