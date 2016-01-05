require "CGI"

module Milkrun
  class I18nStringResources
    def create
      locales.each do |country_code, translations|
        File.open(resource_path(country_code), 'w') do |f|
          f.puts '<?xml version="1.0" encoding="utf-8"?>'
          f.puts '<!-- This file is automatically generated. If you manually modify it, your changes may be overwritten. -->'
          f.puts '<resources>'
          flatten(translations)
            .select { |k, _| default_keys.member?(k) } # Filter out strings in other locales but not default
            .sort
            .map { |k, v| [ k, v.gsub(/'/) { "\\'" } # Escape single quotes
                                .gsub(/&/) { "&amp;" } # Escape ampersands
                                .gsub("\n") { "\\n\n" } # Add explicit new line
                                .gsub(/\A([ ]+)/) { $1.gsub(" ", '\u0020') } # Replace leading spaces with hard-coded space
                                .gsub("<") { "&lt;" } ] } # Escape '<' characters
            .map { |k, v| "  <string name=\"#{k}\" formatted=\"false\">#{v}</string>" }
            .each { |str| f.puts(str) }
          f.puts '</resources>'
        end
      end
    end

    private

    def config
      @config ||= JSON.parse(File.read(server_config_path))
    end

    def default_keys
      @default_keys ||= Set.new(flatten(locales["en"]).keys)
    end

    # Transforms a deeply nested hash into a flattened hash, with keys separated
    # by underscores. e.g.: {a: "b", c: {d: "e"}} becomes: {"a" => "b", "c_d" => "e"}
    def flatten(input, prefix: "")
      input.reduce({}) do |accum, (k, v)|
        key = prefix + k.to_s
        v.is_a?(Hash) ?
          accum.merge(flatten(v, prefix: key + "_")) :
          accum.merge(key => v)
      end
    end

    def locales
      @locales ||= config["locales"]
    end

    def resource_path(country_code)
      values_dir = country_code.to_s == "en" ? "values" : "values-#{country_code}"
      dir = File.expand_path(File.join(Milkrun.app_dir, "src/main/res", values_dir))
      FileUtils.mkdir_p(dir)
      File.join(dir, "strings_i18n.xml")
    end

    def server_config_path
      File.expand_path(File.join(Milkrun.assets_dir, "json/server-config.json"))
    end
  end
end
