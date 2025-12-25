import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/generation_model.dart';

class ForgeService {
  static const String _baseUrl = 'https://api.forgeai.com'; // Placeholder URL

  Future<GenerationResponse> generateApplication(GenerationRequest request) async {
    try {
      final response = await http.post(
        Uri.parse('$_baseUrl/api/generate'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode(request.toJson()),
      );

      if (response.statusCode == 200) {
        return GenerationResponse.fromJson(jsonDecode(response.body));
      } else {
        throw Exception('Failed to generate application: ${response.statusCode}');
      }
    } catch (e) {
      throw Exception('Error connecting to ForgeAI: $e');
    }
  }
}
