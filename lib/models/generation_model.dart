class GenerationRequest {
  final String applicationType;
  final String projectName;
  final String description;
  final String category;
  final List<String> sections;
  final String themeColor;

  GenerationRequest({
    required this.applicationType,
    required this.projectName,
    required this.description,
    required this.category,
    required this.sections,
    required this.themeColor,
  });

  Map<String, dynamic> toJson() {
    return {
      'applicationType': applicationType,
      'projectName': projectName,
      'description': description,
      'category': category,
      'sections': sections,
      'themeColor': themeColor,
    };
  }
}

class GenerationResponse {
  final String html;
  final String css;
  final String js;

  GenerationResponse({
    required this.html,
    required this.css,
    required this.js,
  });

  factory GenerationResponse.fromJson(Map<String, dynamic> json) {
    return GenerationResponse(
      html: json['html'] ?? '',
      css: json['css'] ?? '',
      js: json['js'] ?? '',
    );
  }
}
